/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.*;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public abstract class JournalTest<T extends Journal> {

    protected T journal;
    protected StandardRepository repository;
    private IndexEngine indexEngine;
    protected NTPServerTimeProvider timeProvider;

    @SneakyThrows
    public JournalTest(T journal) {
        this.journal = journal;
    }

    @BeforeClass
    public void setUpEnv() throws Exception {
        repository = new StandardRepository();
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{JournalTest.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{JournalTest.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{EventCausalityEstablished.class
                .getPackage()}));
        repository.setJournal(this.journal);
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);
        repository.setLockProvider(new LocalLockProvider());
        indexEngine = new MemoryIndexEngine();
        repository.setIndexEngine(indexEngine);
        repository.startAsync().awaitRunning();
    }

    @AfterClass
    public void tearDownEnv() throws Exception {
        repository.stopAsync().awaitTerminated();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        journal.clear();
    }

    public static class TestEvent extends StandardEvent {
        @Builder
        public TestEvent(HybridTimestamp timestamp) {
            super(timestamp);
        }
    }

    public static class AnotherTestEvent extends StandardEvent {
        @Builder
        public AnotherTestEvent(HybridTimestamp timestamp) {
            super(timestamp);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    public static class TestCommand extends StandardCommand<Void, Void> {
        @Getter
        private final boolean events;

        @Builder
        public TestCommand(HybridTimestamp timestamp, boolean events) {
            super(timestamp);
            this.events = events;
        }

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            if (events) {
                return EventStream.of(TestEvent.builder().build());
            } else {
                return super.events(repository);
            }
        }
    }

    public static class ExceptionalTestCommand extends StandardCommand<Void, Void> {
        @Builder
        public ExceptionalTestCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            return EventStream.of(Stream.generate(() -> {
                throw new IllegalStateException();
            }));
        }
    }

    @Test
    @SneakyThrows
    public void journalCounting() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        assertEquals(journal.journal(TestCommand.builder().events(true).timestamp(timestamp).build()), 1);
        timestamp.update();
        assertEquals(journal.journal(TestCommand.builder().events(false).timestamp(timestamp).build()), 0);
    }

    @Test
    @SneakyThrows
    public void journalListener() {
        AtomicInteger onEvent = new AtomicInteger(0);
        AtomicBoolean onCommit = new AtomicBoolean(false);
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        assertEquals(1,
                     journal.journal(TestCommand.builder().events(true).timestamp(timestamp).build(), new Journal.Listener() {
                         @Override
                         public void onEvent(Event event) {
                             onEvent.incrementAndGet();
                         }

                         @Override
                         public void onCommit(Command command) {
                             onCommit.set(true);
                         }
                     }));

        assertEquals(onEvent.get(), 2);
        assertTrue(onCommit.get());
    }

    @Test
    @SneakyThrows
    public void journalListenerAbort() {
        AtomicBoolean onAbort = new AtomicBoolean(false);
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        try {
            assertEquals(1, journal.journal(ExceptionalTestCommand.builder().timestamp(timestamp).build(),
                                            new Journal.Listener() {
                                                @Override
                                                public void onAbort(Throwable throwable) {
                                                    onAbort.set(true);
                                                }
                                            }));
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        assertTrue(onAbort.get());
    }

    @Test
    @SneakyThrows
    public void journalRetrieving() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        List<Event> events = new ArrayList<>();
        TestCommand command = TestCommand.builder().events(true).build();
        journal.journal(command.timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });
        assertEquals(events.size(), 2);

        Optional<Entity> entity = journal.get(command.uuid());
        assertTrue(entity.isPresent());
        assertEquals(command.uuid(), entity.get().uuid());

        Event event = events.get(0);
        Optional<Entity> eventEntity = journal.get(event.uuid());
        assertTrue(eventEntity.isPresent());
        assertEquals(event.uuid(), eventEntity.get().uuid());
    }

    @Test
    @SneakyThrows
    public void journalIterating() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        List<Event> events = new ArrayList<>();
        TestCommand command1 = TestCommand.builder().events(true).build();
        TestCommand command2 = TestCommand.builder().events(true).build();
        journal.journal(command1.timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });

        journal.journal(command2.timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });

        Iterator<EntityHandle<TestCommand>> commandIterator = journal.commandIterator(TestCommand.class);

        List<EntityHandle<TestCommand>> commands = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(commandIterator, Spliterator.IMMUTABLE), false)
                .collect(Collectors.toList());

        assertEquals(commands.size(), 2);
        assertTrue(commands.stream().anyMatch(c -> c.uuid().equals(command1.uuid())));
        assertTrue(commands.stream().anyMatch(c -> c.uuid().equals(command2.uuid())));

        assertEquals(events.size(), 4);

        Iterator<EntityHandle<TestEvent>> eventIterator = journal.eventIterator(TestEvent.class);
        List<UUID> iteratedEvents = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(eventIterator, Spliterator.IMMUTABLE), false)
                .map(EntityHandle::uuid)
                .collect(Collectors.toList());
        assertTrue(iteratedEvents.containsAll(events.stream().filter(e -> e instanceof TestEvent)
                                                    .map(Event::uuid)
                                                    .collect(Collectors.toList())));
    }

}