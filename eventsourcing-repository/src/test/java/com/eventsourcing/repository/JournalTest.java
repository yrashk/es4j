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
import com.eventsourcing.inmem.MemoryIndexEngine;
import com.googlecode.cqengine.index.support.CloseableIterator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
        private TestEvent event;

        @Builder
        public TestCommand(HybridTimestamp timestamp, boolean events) {
            super(timestamp);
            this.events = events;
            event = TestEvent.builder().build();
        }

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            if (events) {
                return EventStream.of(event);
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
        public EventStream<Void> events() throws Exception {
            return EventStream.of(Stream.generate(() -> {
                throw new IllegalStateException();
            }));
        }
    }

    @Test
    @SneakyThrows
    public void journalRetrieving() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        TestCommand command = TestCommand.builder().events(true).build();
        command.timestamp(timestamp);
        Journal.Transaction tx = journal.beginTransaction();
        journal.journal(tx, command);
        journal.journal(tx, command.event);

        assertFalse(journal.get(command.uuid()).isPresent());
        assertFalse(journal.get(command.event.uuid()).isPresent());

        tx.commit();

        Optional<Entity> entity = journal.get(command.uuid());
        assertTrue(entity.isPresent());
        assertEquals(command.uuid(), entity.get().uuid());

        Optional<Entity> eventEntity = journal.get(command.event.uuid());
        assertTrue(eventEntity.isPresent());
        assertEquals(command.event.uuid(), eventEntity.get().uuid());
    }

    @Test
    @SneakyThrows
    public void journalIterating() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestCommand command1 = TestCommand.builder().events(true).build();
        TestCommand command2 = TestCommand.builder().events(true).build();

        Journal.Transaction tx = journal.beginTransaction();
        journal.journal(tx, command1);
        journal.journal(tx, command1.event);
        tx.commit();

        tx = journal.beginTransaction();
        journal.journal(tx, command2);
        journal.journal(tx, command2.event);
        tx.commit();

        CloseableIterator<EntityHandle<TestCommand>> commandIterator = journal.commandIterator(TestCommand.class);

        List<EntityHandle<TestCommand>> commands = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(commandIterator, Spliterator.IMMUTABLE), false)
                .collect(Collectors.toList());

        commandIterator.close();

        assertEquals(commands.size(), 2);
        assertTrue(commands.stream().anyMatch(c -> c.uuid().equals(command1.uuid())));
        assertTrue(commands.stream().anyMatch(c -> c.uuid().equals(command2.uuid())));

        CloseableIterator<EntityHandle<TestEvent>> eventIterator = journal.eventIterator(TestEvent.class);
        List<UUID> iteratedEvents = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(eventIterator, Spliterator.IMMUTABLE), false)
                .map(EntityHandle::uuid)
                .collect(Collectors.toList());
        eventIterator.close();
        assertTrue(iteratedEvents.containsAll(Arrays.asList(command1.event.uuid(), command2.event.uuid())));
    }

}