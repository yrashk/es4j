/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import boguspackage.BogusCommand;
import boguspackage.BogusEvent;
import com.eventsourcing.*;
import com.eventsourcing.events.CommandTerminatedExceptionally;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.events.JavaExceptionOccurred;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.*;
import com.eventsourcing.inmem.MemoryIndexEngine;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.migrations.events.EntityLayoutIntroduced;
import com.eventsourcing.repository.commands.IntroduceEntityLayouts;
import com.google.common.collect.Iterables;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.net.ntp.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.eventsourcing.queries.QueryFactory.*;
import static com.eventsourcing.index.IndexEngine.IndexFeature.*;
import static org.testng.Assert.*;

public abstract class RepositoryTest<T extends Repository> {

    private final T repository;
    private Journal journal;
    private IndexEngine indexEngine;
    private LocalLockProvider lockProvider;
    private NTPServerTimeProvider timeProvider;
    private TimeStamp startTime;

    public RepositoryTest(T repository) {
        this.repository = repository;
    }

    @BeforeClass
    public void setUpEnv() throws Exception {
        startTime = new TimeStamp(new Date());
        repository
                .addCommandSetProvider(new PackageCommandSetProvider(new Package[]{RepositoryTest.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{RepositoryTest.class.getPackage()}));
        journal = createJournal();
        repository.setJournal(journal);
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);
        indexEngine = createIndexEngine();
        repository.setIndexEngine(indexEngine);
        lockProvider = new LocalLockProvider();
        repository.setLockProvider(lockProvider);
        repository.startAsync().awaitRunning();

        long size = journal.size(EntityLayoutIntroduced.class);
        assertTrue(size > 0);
        // make sure layout introductions don't duplicate
        repository.publish(new IntroduceEntityLayouts(Iterables.concat(repository.getCommands(), repository.getEvents()))).join();
        assertEquals(journal.size(EntityLayoutIntroduced.class), size);
    }

    protected IndexEngine createIndexEngine() {return new MemoryIndexEngine();}

    protected abstract Journal createJournal();

    @AfterClass
    public void tearDownEnv() throws Exception {
        repository.stopAsync().awaitTerminated();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        journal.clear();
        indexEngine.getIndexedCollection(EntityLayoutIntroduced.class).clear();
        repository.publish(new IntroduceEntityLayouts(Iterables.concat(repository.getCommands(), repository.getEvents()))).join();
    }

    @Accessors(fluent = true) @ToString
    @Indices({TestEvent.class, TestEventExtraIndices.class})
    public static class TestEvent extends StandardEvent {
        @Getter
        private final String string;

        @Index({EQ, SC})
        public final static SimpleIndex<TestEvent, String> ATTR = SimpleIndex.as(TestEvent::string);

        @Index({EQ, SC})
        @Deprecated
        public final static SimpleIndex<TestEvent, String> ATTR_DEP = SimpleIndex.as(TestEvent::string);

        @Index
        public final static MultiValueIndex<TestEvent, String> ATTRS = MultiValueIndex.as(TestEvent::strings);

        public List<String> strings() {
            return Arrays.asList(string);
        }

        @Builder
        public TestEvent(HybridTimestamp timestamp, String string) {
            super(timestamp);
            this.string = string;
        }
    }

    public static class TestEventExtraIndices {
        public static SimpleIndex<TestEvent, List<String>> ATTRL = TestEvent::strings;
    }

    @ToString
    public static class RepositoryTestCommand extends StandardCommand<Void, String> {

        @Getter
        private final String value;

        @Builder
        public RepositoryTestCommand(HybridTimestamp timestamp, String value) {
            super(timestamp);
            this.value = value == null ? "test" : value;
        }


        @Override
        public EventStream<Void> events() {
            return EventStream.of(TestEvent.builder().string(value).build());
        }

        @Override
        public String result() {
            return "hello, world";
        }

        @Index({EQ, SC})
        public final static SimpleIndex<RepositoryTestCommand, String> ATTR = SimpleIndex.as(RepositoryTestCommand::getValue);

    }

    @Test @SneakyThrows
    public void initialTimestamp() {
        HybridTimestamp t = repository.getTimestamp();
        long ts = t.timestamp();
        TimeStamp soon = new TimeStamp(new Date(new Date().toInstant().plus(1, ChronoUnit.SECONDS).toEpochMilli()));
        TimeStamp t1 = new TimeStamp(ts);
        assertTrue(HybridTimestamp.compare(t1, startTime) > 0);
        assertTrue(HybridTimestamp.compare(t1, soon) < 0);
    }

    @Test
    public void discovery() {
        assertTrue(repository.getCommands().contains(RepositoryTestCommand.class));
    }

    @Test
    @SneakyThrows
    public void layoutIntroduction() {
        try (ResultSet<EntityHandle<EntityLayoutIntroduced>> resultSet = repository
                .query(EntityLayoutIntroduced.class, all(EntityLayoutIntroduced.class))) {
            assertTrue(resultSet.isNotEmpty());
        }
    }

    @Test
    @SneakyThrows
    public void basicPublish() {
        assertEquals("hello, world", repository.publish(RepositoryTestCommand.builder().build()).get());
    }

    @Test
    @SneakyThrows
    public void subscribe() {
        final AtomicBoolean gotEvent = new AtomicBoolean();
        final AtomicBoolean gotCommand = new AtomicBoolean();
        repository.addEntitySubscriber(new ClassEntitySubscriber<TestEvent>(TestEvent.class) {
            @Override public void onEntity(EntityHandle<TestEvent> entity) {
                gotEvent.set(journal.get(entity.uuid()).isPresent());
            }
        });
        repository.addEntitySubscriber(new ClassEntitySubscriber<RepositoryTestCommand>(RepositoryTestCommand.class) {
            @Override public void onEntity(EntityHandle<RepositoryTestCommand> entity) {
                gotCommand.set(journal.get(entity.uuid()).isPresent());
            }
        });
        repository.publish(RepositoryTestCommand.builder().build()).get();
        assertTrue(gotEvent.get());
        assertTrue(gotCommand.get());
    }

    @Test
    @SneakyThrows
    public void timestamping() {
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        coll.clear();
        IndexedCollection<EntityHandle<RepositoryTestCommand>> coll1 = indexEngine
                .getIndexedCollection(RepositoryTestCommand.class);
        coll1.clear();

        repository.publish(RepositoryTestCommand.builder().build()).get();
        TestEvent test = coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult().get();
        assertNotNull(test.timestamp());

        RepositoryTestCommand test1 = coll1.retrieve(equal(RepositoryTestCommand.ATTR, "test")).uniqueResult().get();
        assertNotNull(test1.timestamp());

        assertTrue(test.timestamp().compareTo(test1.timestamp()) > 0);
    }

    @Test
    @SneakyThrows
    public void commandTimestamping() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        RepositoryTestCommand command1 = RepositoryTestCommand.builder().value("forced")
                                                              .timestamp(timestamp)
                                                              .build();
        RepositoryTestCommand command2 = RepositoryTestCommand.builder().build();
        IndexedCollection<EntityHandle<RepositoryTestCommand>> coll1 = indexEngine
                .getIndexedCollection(RepositoryTestCommand.class);
        coll1.clear();

        repository.publish(command2).get();
        repository.publish(command1).get();

        RepositoryTestCommand test1 = coll1.retrieve(equal(RepositoryTestCommand.ATTR, "forced")).uniqueResult().get();
        RepositoryTestCommand test2 = coll1.retrieve(equal(RepositoryTestCommand.ATTR, "test")).uniqueResult().get();

        assertTrue(test1.timestamp().compareTo(test2.timestamp()) < 0);
        assertTrue(repository.getTimestamp().compareTo(test1.timestamp()) > 0);
        assertTrue(repository.getTimestamp().compareTo(test2.timestamp()) > 0);
    }

    @ToString
    public static class TimestampingEventCommand extends StandardCommand<Void, String> {

        private final HybridTimestamp eventTimestamp;

        @LayoutConstructor
        public TimestampingEventCommand(HybridTimestamp timestamp) {
            super(timestamp);
            eventTimestamp = null;
        }

        @Builder
        public TimestampingEventCommand(HybridTimestamp timestamp, HybridTimestamp eventTimestamp) {
            super(timestamp);
            this.eventTimestamp = eventTimestamp;
        }


        @Override
        public EventStream<Void> events() {
            return EventStream.of(new Event[]{
                                  TestEvent.builder().string("test").timestamp(eventTimestamp).build(),
                                  TestEvent.builder().string("followup").build()});
        }

        @Override
        public String result() {
            return "hello, world";
        }
    }

    @Test
    @SneakyThrows
    public void eventTimestamping() {
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine
                .getIndexedCollection(TestEvent.class);
        coll.clear();

        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        TimestampingEventCommand command = TimestampingEventCommand.builder().eventTimestamp(timestamp).build();

        repository.publish(command).get();

        TestEvent test = coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult().get();

        assertTrue(test.timestamp().compareTo(command.timestamp()) < 0);
        assertTrue(repository.getTimestamp().compareTo(test.timestamp()) > 0);

        TestEvent followup = coll.retrieve(equal(TestEvent.ATTR, "followup")).uniqueResult().get();
        assertTrue(test.timestamp().compareTo(followup.timestamp()) < 0);

        assertTrue(repository.getTimestamp().compareTo(followup.timestamp()) > 0);
    }

    @Test
    @SneakyThrows
    public void indexing() {
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        coll.clear();
        IndexedCollection<EntityHandle<RepositoryTestCommand>> coll1 = indexEngine
                .getIndexedCollection(RepositoryTestCommand.class);
        coll1.clear();

        repository.publish(RepositoryTestCommand.builder().build()).get();
        assertTrue(coll.retrieve(equal(TestEvent.ATTR, "test")).isNotEmpty());
        assertTrue(coll.retrieve(equal(TestEvent.ATTR_DEP, "test")).isNotEmpty());
        assertTrue(coll.retrieve(contains(TestEvent.ATTR, "es")).isNotEmpty());
        assertEquals(coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult().get().string(), "test");
        assertEquals(coll.retrieve(equal(TestEvent.ATTRS, "test")).uniqueResult().get().string(), "test");
        assertEquals(coll.retrieve(equal(TestEventExtraIndices.ATTRL, Arrays.asList("test"))).uniqueResult().get().string(),
                     "test");
        assertTrue(coll.retrieve(equal(TestEventExtraIndices.ATTRL, Arrays.asList("test1"))).isEmpty());

        assertTrue(coll1.retrieve(equal(RepositoryTestCommand.ATTR, "test")).isNotEmpty());
        assertTrue(coll1.retrieve(contains(RepositoryTestCommand.ATTR, "es")).isNotEmpty());

    }

    @Accessors(fluent = true) @ToString
    public static class TestEventWithQueryOptions extends StandardEvent {
        @Getter
        private final String string;

        @Index({EQ, SC})
        public static SimpleIndex<TestEventWithQueryOptions, String> ATTR =
                SimpleIndex.withQueryOptions((object, queryOptions) -> {
                    if (queryOptions.get(TestEventWithQueryOptions.class) != null) {
                        return "QueryOptions";
                    } else {
                        return object.string();
                    }
                });


        @Builder
        public TestEventWithQueryOptions(String string) {
            this.string = string;
        }
    }


    @ToString
    public static class TestEventWithQueryOptionsCommand extends StandardCommand<Void, String> {

        @Getter
        private final String value;

        @Builder
        public TestEventWithQueryOptionsCommand(HybridTimestamp timestamp, String value) {
            super(timestamp);
            this.value = value == null ? "test" : value;
        }


        @Override
        public EventStream<Void> events() {
            return EventStream.of(TestEventWithQueryOptions.builder().string(value).build());
        }

        @Override
        public String result() {
            return "hello, world";
        }
    }

    @Test
    @SneakyThrows
    public void queryOptionsInIndices() {
        IndexedCollection<EntityHandle<TestEventWithQueryOptions>> coll =
                indexEngine.getIndexedCollection(TestEventWithQueryOptions.class);
        coll.clear();

        repository.publish(TestEventWithQueryOptionsCommand.builder().build()).get();
        assertTrue(coll.retrieve(equal(TestEventWithQueryOptions.ATTR, "test")).isNotEmpty());

    }

    @Test
    @SneakyThrows
    public void publishingNewCommand() {
        assertFalse(repository.getCommands().contains(BogusCommand.class));
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{BogusCommand.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{BogusEvent.class.getPackage()}));
        assertTrue(repository.getCommands().contains(BogusCommand.class));
        assertTrue(repository.getEvents().contains(BogusEvent.class));
        Assert.assertEquals("bogus", repository.publish(BogusCommand.builder().build()).get());
        // testing its indexing
        IndexedCollection<EntityHandle<BogusEvent>> coll = indexEngine.getIndexedCollection(BogusEvent.class);
        assertTrue(coll.retrieve(equal(BogusEvent.ATTR, "bogus")).isNotEmpty());
        assertTrue(coll.retrieve(contains(BogusEvent.ATTR, "us")).isNotEmpty());
        Assert.assertEquals(coll.retrieve(equal(BogusEvent.ATTR, "bogus")).uniqueResult().get().string(), "bogus");
    }

    public static class LockCommand extends StandardCommand<Void, Void> {
        @Builder
        public LockCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override
        public EventStream<Void> events(Repository repository, LockProvider lockProvider) {
            lockProvider.lock("LOCK");
            return EventStream.empty();
        }
    }

    @Test(timeOut = 1000) @SneakyThrows
    public void lockTracking() {
        repository.publish(LockCommand.builder().build()).get();
        Lock lock = lockProvider.lock("LOCK");
        assertTrue(lock.isLocked());
        lock.unlock();
    }

    public static class ExceptionalLockCommand extends StandardCommand<Void, Void> {
        @Builder
        public ExceptionalLockCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override
        public EventStream<Void> events(Repository repository, LockProvider lockProvider) {
            lockProvider.lock("LOCK");
            throw new IllegalStateException();
        }
    }

    @Test(timeOut = 1000) @SneakyThrows
    public void exceptionalLockTracking() {
        repository.publish(ExceptionalLockCommand.builder().build()).exceptionally(throwable -> null).get();
        Lock lock = lockProvider.lock("LOCK");
        assertTrue(lock.isLocked());
        lock.unlock();
    }


    public static class ExceptionalCommand extends StandardCommand<Void, Object> {
        @Getter
        private boolean resultCalled = false;

        @Builder
        public ExceptionalCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override
        public EventStream<Void> events() {
            throw new IllegalStateException();
        }


        @Override public Object result() {
            resultCalled = true;
            return null;
        }
    }

    @Test @SneakyThrows
    public void exceptionalCommand() {
        ExceptionalCommand command = ExceptionalCommand.builder().build();
        Object o = repository.publish(command).exceptionally(throwable -> throwable).get();
        assertTrue(o instanceof IllegalStateException);
        Optional<Entity> commandLookup = journal.get(command.uuid());
        assertTrue(commandLookup.isPresent());
        assertTrue(command.hasTerminatedExceptionally(repository));
        // result() was not called
        assertFalse(command.isResultCalled());
        try (ResultSet<EntityHandle<CommandTerminatedExceptionally>> resultSet = repository
                .query(CommandTerminatedExceptionally.class,
                       and(all(CommandTerminatedExceptionally.class),
                           existsIn(
                                   indexEngine.getIndexedCollection(EventCausalityEstablished.class),
                                   CommandTerminatedExceptionally.ID, EventCausalityEstablished.EVENT)))) {
            assertEquals(resultSet.size(), 1);
        }
        assertEquals(command.exceptionalTerminationCause(repository).getClassName(), IllegalStateException.class
                .getName());
        try (ResultSet<EntityHandle<JavaExceptionOccurred>> resultSet = repository
                .query(JavaExceptionOccurred.class,
                       and(all(JavaExceptionOccurred.class),
                           existsIn(
                                   indexEngine.getIndexedCollection(EventCausalityEstablished.class),
                                   JavaExceptionOccurred.ID, EventCausalityEstablished.EVENT)))) {
            assertEquals(resultSet.size(), 1);
            EntityHandle<JavaExceptionOccurred> result = resultSet.uniqueResult();
            assertEquals(result.get().getClassName(), IllegalStateException.class.getName());
        }

    }


    @ToString
    public static class StreamExceptionCommand extends StandardCommand<Void, Void> {

        private final UUID eventUUID;

        @Getter
        private boolean resultCalled = false;

        @LayoutConstructor
        public StreamExceptionCommand(HybridTimestamp timestamp) {
            super(timestamp);
            eventUUID = null;
        }

        @Builder
        public StreamExceptionCommand(HybridTimestamp timestamp, UUID eventUUID) {
            super(timestamp);
            this.eventUUID = eventUUID == null ? UUID.randomUUID() : eventUUID;
        }


        @Override
        public EventStream<Void> events() {
            return EventStream.of(Stream.concat(Stream.of(
                    TestEvent.builder().string("test").build().uuid(eventUUID)),
                                 Stream.generate(() -> {
                                     throw new IllegalStateException();
                                 })));
        }

        @Override public Void result() {
            resultCalled = true;
            return null;
        }
    }

    @Test
    @SneakyThrows
    public void streamExceptionIndexing() {
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        coll.clear();
        UUID eventUUID = UUID.randomUUID();
        StreamExceptionCommand command = StreamExceptionCommand.builder().eventUUID(eventUUID).build();
        CompletableFuture<Void> future = repository.publish(command);
        while (!future.isDone()) { Thread.sleep(10); } // to avoid throwing an exception
        assertTrue(future.isCompletedExceptionally());
        // result() was not called
        assertFalse(command.isResultCalled());
        try (ResultSet<EntityHandle<CommandTerminatedExceptionally>> resultSet = repository
                .query(CommandTerminatedExceptionally.class,
                       and(all(CommandTerminatedExceptionally.class),
                           existsIn(
                                   indexEngine.getIndexedCollection(EventCausalityEstablished.class),
                                   CommandTerminatedExceptionally.ID, EventCausalityEstablished.EVENT)))) {
            assertEquals(resultSet.size(), 1);
        }
        assertTrue(journal.get(command.uuid()).isPresent());
        assertFalse(journal.get(eventUUID).isPresent());
        assertTrue(repository.query(TestEvent.class, equal(TestEvent.ATTR, "test")).isEmpty());
    }


    public static class StatePassageCommand extends StandardCommand<String, String> {

        @Builder
        public StatePassageCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override
        public EventStream<String> events(Repository repository, LockProvider lockProvider) throws Exception {
            return EventStream.empty("hello");
        }

        @Override
        public String result(String state) {
            return state;
        }
    }

    @Test @SneakyThrows
    public void statePassage() {
        String s = repository.publish(StatePassageCommand.builder().build()).get();
        assertEquals(s, "hello");
    }

    public static class PassingLockProvider extends StandardCommand<Boolean, Boolean> {

        @Builder
        public PassingLockProvider(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override
        public EventStream<Boolean> events(Repository repository, LockProvider lockProvider) throws Exception {
            return EventStream.empty(lockProvider != null);
        }

        @Override
        public Boolean result(Boolean passed) {
            return passed;
        }
    }

    @Test @SneakyThrows
    public void passingLock() {
        assertTrue(repository.publish(PassingLockProvider.builder().build()).get());
    }

    @Accessors(fluent = true) @ToString
    public static class TestOptionalEvent extends StandardEvent {
        @Getter
        private final Optional<String> optional;

        @Index({EQ, UNIQUE})
        public final static SimpleIndex<TestOptionalEvent, UUID> ATTR = SimpleIndex.as(StandardEntity::uuid);

        @Builder
        public TestOptionalEvent(Optional<String> optional) {
            this.optional = optional;
        }
    }

    @Accessors(fluent = true)
    @ToString
    public static class TestOptionalCommand extends StandardCommand<Void, Void> {
        @Getter
        private final Optional<String> optional;

        @Builder
        public TestOptionalCommand(HybridTimestamp timestamp, Optional<String> optional) {
            super(timestamp);
            this.optional = optional;
        }

        @Override
        public EventStream<Void> events() {
            return EventStream.of(TestOptionalEvent.builder().optional(optional).build());
        }

        @Index({EQ, UNIQUE})
        public final static SimpleIndex<TestOptionalCommand, UUID> ATTR = SimpleIndex.as(StandardEntity::uuid);

    }

    @Test @SneakyThrows
    public void commandGoesThroughLayoutSerialization() {
        TestOptionalCommand command = TestOptionalCommand.builder().build();
        repository.publish(command).get();

        TestOptionalCommand test = repository
                .query(TestOptionalCommand.class, equal(TestOptionalCommand.ATTR, command.uuid())).uniqueResult().get();
        assertFalse(test.optional().isPresent());

    }

    @Test @SneakyThrows
    public void eventGoesThroughLayoutSerialization() {
        TestOptionalCommand command = TestOptionalCommand.builder().build();
        repository.publish(command).get();

        TestOptionalEvent testOptionalEvent = repository.query(TestOptionalEvent.class, all(TestOptionalEvent.class))
                                                        .uniqueResult().get();
        assertFalse(testOptionalEvent.optional().isPresent());
    }

    @Test @SneakyThrows
    public void causalRelationship() {
        RepositoryTestCommand command = RepositoryTestCommand.builder().build();
        repository.publish(command).get();
        try (ResultSet<EntityHandle<EventCausalityEstablished>> resultSet = repository
                .query(EventCausalityEstablished.class, equal(EventCausalityEstablished.COMMAND, command.uuid()))) {
            assertEquals(resultSet.size(), 1);
        }
    }

    public static class LongRunningCommandEvents extends StandardCommand<Void, Void> {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        @Override public EventStream<Void> events() throws Exception {
            future.get();
            return EventStream.empty();
        }
    }

    @Test
    @SneakyThrows
    public void longRunningCommandEventsShouldNotBlock() {
        LongRunningCommandEvents command = new LongRunningCommandEvents();
        repository.publish(command);
        repository.publish(RepositoryTestCommand.builder().value("1").build()).get(5_000, TimeUnit.MILLISECONDS);
        command.future.complete(null);
    }

    public static class LongRunningCommandEventStreamGeneration extends StandardCommand<Void, Void> {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        @Override public EventStream<Void> events() throws Exception {
            return EventStream.of(Stream.of(1).map(i -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return TestEvent.builder().build();}));
        }
    }

    @Test
    @SneakyThrows
    public void longRunningCommandEventStreamGenerationShouldNotBlock() {
        LongRunningCommandEventStreamGeneration command = new LongRunningCommandEventStreamGeneration();
        repository.publish(command);
        repository.publish(RepositoryTestCommand.builder().value("1").build()).get(5_000, TimeUnit.MILLISECONDS);
        command.future.complete(null);
    }

    @Test
    @SneakyThrows
    public void longRunningCommandEventStreamGenerationSameCommandShouldNotBlock() {
        LongRunningCommandEventStreamGeneration cmd1 = new LongRunningCommandEventStreamGeneration();
        repository.publish(cmd1);
        LongRunningCommandEventStreamGeneration cmd2 = new LongRunningCommandEventStreamGeneration();
        cmd2.future.complete(null);
        repository.publish(cmd2).get(5_000, TimeUnit.MILLISECONDS);
        cmd1.future.complete(null);
    }

}