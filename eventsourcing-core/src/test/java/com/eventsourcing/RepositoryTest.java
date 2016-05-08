/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import boguspackage.BogusCommand;
import boguspackage.BogusEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.events.CommandTerminatedExceptionally;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.eventsourcing.index.EntityQueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.contains;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static org.testng.Assert.*;

public abstract class RepositoryTest<T extends Repository> {

    private final T repository;
    private Journal journal;
    private MemoryIndexEngine indexEngine;
    private MemoryLockProvider lockProvider;

    public RepositoryTest(T repository) {
        this.repository = repository;
    }

    @BeforeClass
    public void setUpEnv() throws Exception {
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{RepositoryTest.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{RepositoryTest.class.getPackage()}));
        journal = createJournal();
        repository.setJournal(journal);
        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);
        indexEngine = new MemoryIndexEngine();
        repository.setIndexEngine(indexEngine);
        lockProvider = new MemoryLockProvider();
        repository.setLockProvider(lockProvider);
        repository.startAsync().awaitRunning();
    }

    protected abstract Journal createJournal();

    @AfterClass
    public void tearDownEnv() throws Exception {
        repository.stopAsync().awaitTerminated();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        journal.clear();
    }

    @Accessors(fluent = true) @ToString
    public static class TestEvent extends Event {
        @Getter @Setter
        private String string;

        @Index({IndexEngine.IndexFeature.EQ, IndexEngine.IndexFeature.SC})
        public static SimpleAttribute<TestEvent, String> ATTR = new SimpleAttribute<TestEvent, String>() {
            @Override
            public String getValue(TestEvent object, QueryOptions queryOptions) {
                return object.string();
            }
        };
    }

    @ToString
    public static class RepositoryTestCommand extends Command<String> {
        @Override
        public Stream<Event> events(Repository repository) {
            return Stream.of(new TestEvent().string("test"));
        }

        @Override
        public String onCompletion() {
            return "hello, world";
        }

        @Index({IndexEngine.IndexFeature.EQ, IndexEngine.IndexFeature.SC})
        public static SimpleAttribute<RepositoryTestCommand, String> ATTR = new SimpleAttribute<RepositoryTestCommand, String>() {
            @Override
            public String getValue(RepositoryTestCommand object, QueryOptions queryOptions) {
                return "test";
            }
        };

    }

    @Test
    public void discovery() {
        assertTrue(repository.getCommands().contains(RepositoryTestCommand.class));
    }

    @Test
    @SneakyThrows
    public void basicPublish() {
        assertEquals("hello, world", repository.publish(new RepositoryTestCommand()).get());
    }

    @Test
    @SneakyThrows
    public void timestamping() {
        repository.publish(new RepositoryTestCommand()).get();
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        TestEvent test = coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult().get();
        assertNotNull(test.timestamp());

        IndexedCollection<EntityHandle<RepositoryTestCommand>> coll1 = indexEngine.getIndexedCollection(RepositoryTestCommand.class);
        RepositoryTestCommand test1 = coll1.retrieve(equal(RepositoryTestCommand.ATTR, "test")).uniqueResult().get();
        assertNotNull(test1.timestamp());

        assertTrue(test.timestamp().compareTo(test1.timestamp()) > 0);
    }

    @Test
    @SneakyThrows
    public void indexing() {
        repository.publish(new RepositoryTestCommand()).get();
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        assertTrue(coll.retrieve(equal(TestEvent.ATTR, "test")).isNotEmpty());
        assertTrue(coll.retrieve(contains(TestEvent.ATTR, "es")).isNotEmpty());
        assertEquals(coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult().get().string(), "test");

        IndexedCollection<EntityHandle<RepositoryTestCommand>> coll1 = indexEngine.getIndexedCollection(RepositoryTestCommand.class);
        assertTrue(coll1.retrieve(equal(RepositoryTestCommand.ATTR, "test")).isNotEmpty());
        assertTrue(coll1.retrieve(contains(RepositoryTestCommand.ATTR, "es")).isNotEmpty());

    }

    @Test
    @SneakyThrows
    public void publishingNewCommand() {
        assertFalse(repository.getCommands().contains(BogusCommand.class));
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{BogusCommand.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{BogusCommand.class.getPackage()}));
        assertTrue(repository.getCommands().contains(BogusCommand.class));
        assertEquals("bogus", repository.publish(new BogusCommand()).get());
        // testing its indexing
        IndexedCollection<EntityHandle<BogusEvent>> coll = indexEngine.getIndexedCollection(BogusEvent.class);
        assertTrue(coll.retrieve(equal(BogusEvent.ATTR, "bogus")).isNotEmpty());
        assertTrue(coll.retrieve(contains(BogusEvent.ATTR, "us")).isNotEmpty());
        assertEquals(coll.retrieve(equal(BogusEvent.ATTR, "bogus")).uniqueResult().get().string(), "bogus");
    }

    public static class LockCommand extends Command<Void> {
        @Override
        public Stream<Event> events(Repository repository, LockProvider lockProvider) {
            lockProvider.lock("LOCK");
            return Stream.empty();
        }
    }

    @Test(timeOut = 1000) @SneakyThrows
    public void lockTracking() {
        repository.publish(new LockCommand()).get();
        Lock lock = lockProvider.lock("LOCK");
        assertTrue(lock.isLocked());
        lock.unlock();
    }

    public static class ExceptionalLockCommand extends Command<Void> {
        @Override
        public Stream<Event> events(Repository repository, LockProvider lockProvider) {
            lockProvider.lock("LOCK");
            throw new IllegalStateException();
        }
    }

    @Test(timeOut = 1000) @SneakyThrows
    public void exceptionalLockTracking() {
        repository.publish(new ExceptionalLockCommand()).exceptionally(throwable -> null).get();
        Lock lock = lockProvider.lock("LOCK");
        assertTrue(lock.isLocked());
        lock.unlock();
    }


    public static class ExceptionalCommand extends Command<Object> {
        @Override
        public Stream<Event> events(Repository repository) {
            throw new IllegalStateException();
        }
    }

    @Test @SneakyThrows
    public void exceptionalCommand() {
        ExceptionalCommand command = new ExceptionalCommand();
        Object o = repository.publish(command).exceptionally(throwable -> throwable).get();
        assertTrue(o instanceof IllegalStateException);
        Optional<Entity> commandLookup = journal.get(command.uuid());
        assertTrue(commandLookup.isPresent());
        ResultSet<EntityHandle<CommandTerminatedExceptionally>> resultSet = repository.query(CommandTerminatedExceptionally.class, equal(CommandTerminatedExceptionally.COMMAND_ID, command.uuid()));
        assertEquals(resultSet.size(), 1);
        EntityHandle<CommandTerminatedExceptionally> result = resultSet.uniqueResult();
        assertEquals(result.get().className(), IllegalStateException.class.getName());
    }


    @ToString
    public static class StreamExceptionCommand extends Command<Void> {

        private UUID eventUUID = UUID.randomUUID();

        public StreamExceptionCommand() {
        }

        public StreamExceptionCommand(UUID eventUUID) {
            this.eventUUID = eventUUID;
        }

        @Override
        public Stream<Event> events(Repository repository) {
            return Stream.concat(Stream.of(
                    (TestEvent) new TestEvent().string("test").uuid(eventUUID)),
                    Stream.generate((Supplier<Event>) () -> {
                        throw new IllegalStateException();
                    }));
        }
    }
    @Test
    @SneakyThrows
    public void streamExceptionIndexing() {
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        coll.clear();
        UUID eventUUID = UUID.randomUUID();
        StreamExceptionCommand command = new StreamExceptionCommand(eventUUID);
        CompletableFuture<Void> future = repository.publish(command);
        while (!future.isDone()) { Thread.sleep(10); } // to avoid throwing an exception
        assertTrue(future.isCompletedExceptionally());
        ResultSet<EntityHandle<CommandTerminatedExceptionally>> resultSet = repository.query(CommandTerminatedExceptionally.class, equal(CommandTerminatedExceptionally.COMMAND_ID, command.uuid()));
        assertEquals(resultSet.size(), 1);
        EntityHandle<CommandTerminatedExceptionally> result = resultSet.uniqueResult();
        assertEquals(result.get().className(), IllegalStateException.class.getName());
        assertTrue(journal.get(command.uuid()).isPresent());
        assertFalse(journal.get(eventUUID).isPresent());
        assertTrue(repository.query(TestEvent.class, equal(TestEvent.ATTR, "test")).isEmpty());
    }


    public static class SameInstanceCommand extends Command<String> {
        @Getter
        private String field;

        @Override
        public Stream<Event> events(Repository repository, LockProvider lockProvider) throws Exception {
            this.field = "hello";
            return super.events(repository, lockProvider);
        }

        @Override
        public String onCompletion() {
            return field;
        }
    }

    @Test @SneakyThrows
    public void sameInstanceCompletion() {
        String s = repository.publish(new SameInstanceCommand()).get();
        assertEquals(s, "hello");
    }

    public static class PassingLockProvider extends Command<Boolean> {
        @Getter
        private boolean passed = false;

        @Override
        public Stream<Event> events(Repository repository, LockProvider lockProvider) throws Exception {
            this.passed = lockProvider != null;
            return super.events(repository, lockProvider);
        }

        @Override
        public Boolean onCompletion() {
            return passed;
        }
    }

    @Test @SneakyThrows
    public void passingLock() {
        assertTrue(repository.publish(new PassingLockProvider()).get());
    }

    @Accessors(fluent = true) @ToString
    public static class TestOptionalEvent extends Event {
        @Getter @Setter
        private Optional<String> optional;

        @Index({IndexEngine.IndexFeature.EQ, IndexEngine.IndexFeature.UNIQUE})
        public static SimpleAttribute<TestOptionalEvent, UUID> ATTR = new SimpleAttribute<TestOptionalEvent, UUID>() {
            @Override
            public UUID getValue(TestOptionalEvent object, QueryOptions queryOptions) {
                return object.uuid();
            }
        };
    }

    @Accessors(fluent = true)
    @ToString
    public static class TestOptionalCommand extends Command<Void> {
        @Getter @Setter
        private Optional<String> optional;
        @Override
        public Stream<Event> events(Repository repository) {
            return Stream.of(new TestOptionalEvent());
        }

        @Index({IndexEngine.IndexFeature.EQ, IndexEngine.IndexFeature.UNIQUE})
        public static SimpleAttribute<TestOptionalCommand, UUID> ATTR = new SimpleAttribute<TestOptionalCommand, UUID>() {
            @Override
            public UUID getValue(TestOptionalCommand object, QueryOptions queryOptions) {
                return object.uuid();
            }
        };

    }
    @Test @SneakyThrows
    public void goesThroughLayoutSerialization() {
        TestOptionalCommand command = new TestOptionalCommand();
        repository.publish(command).get();

        TestOptionalCommand test = repository.query(TestOptionalCommand.class, equal(TestOptionalCommand.ATTR, command.uuid())).uniqueResult().get();
        assertFalse(test.optional().isPresent());

        TestOptionalEvent testOptionalEvent = repository.query(TestOptionalEvent.class, all(TestOptionalEvent.class)).uniqueResult().get();
        assertFalse(testOptionalEvent.optional().isPresent());
    }

}