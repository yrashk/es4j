/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.repository.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
    private RepositoryImpl repository;
    private IndexEngine indexEngine;
    protected NTPServerTimeProvider timeProvider;

    @SneakyThrows
    public JournalTest(T journal) {
        this.journal = journal;
    }

    @BeforeClass
    public void setUpEnv() throws Exception {
        repository = new RepositoryImpl();
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{JournalTest.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{JournalTest.class.getPackage()}));
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

    public static class TestEvent extends StandardEvent {}

    public static class AnotherTestEvent extends StandardEvent {}

    @EqualsAndHashCode(callSuper = false)
    public static class TestCommand extends StandardCommand<Void, Void> {
        @Getter @Setter
        private boolean events;

        public TestCommand() {
        }

        public TestCommand(boolean events) {
            this.events = events;
        }

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            if (events) {
                return EventStream.of(new TestEvent());
            } else {
                return super.events(repository);
            }
        }
    }

    public static class ExceptionalTestCommand extends StandardCommand<Void, Void> {
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
        assertEquals(journal.journal(new TestCommand(true).timestamp(timestamp)), 1);
        timestamp.update();
        assertEquals(journal.journal(new TestCommand(false).timestamp(timestamp)), 0);
    }

    @Test
    @SneakyThrows
    public void journalListener() {
        AtomicInteger onEvent = new AtomicInteger(0);
        AtomicBoolean onCommit = new AtomicBoolean(false);
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        assertEquals(1,
                     journal.journal(new TestCommand(true).timestamp(timestamp), new Journal.Listener() {
                         @Override
                         public void onEvent(Event event) {
                             onEvent.incrementAndGet();
                         }

                         @Override
                         public void onCommit() {
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
            assertEquals(1, journal.journal(new ExceptionalTestCommand().timestamp(timestamp),
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
        TestCommand command = new TestCommand(true);
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
        TestCommand command1 = new TestCommand(true);
        TestCommand command2 = new TestCommand(true);
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