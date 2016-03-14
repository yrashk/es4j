/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.eventchain.hlc.HybridTimestamp;
import org.eventchain.hlc.NTPServerTimeProvider;
import org.eventchain.index.IndexEngine;
import org.eventchain.index.MemoryIndexEngine;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public abstract class JournalTest<T extends Journal> {

    protected final T journal;
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
        repository.setPackage(JournalTest.class.getPackage());
        repository.setJournal(this.journal);
        timeProvider = new NTPServerTimeProvider();
        repository.setPhysicalTimeProvider(timeProvider);
        repository.setLockProvider(new MemoryLockProvider());
        indexEngine = new MemoryIndexEngine();
        indexEngine.setJournal(journal);
        indexEngine.setRepository(repository);
        repository.setIndexEngine(indexEngine);
        this.journal.setRepository(repository);
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

    public static class TestEvent extends Event {}

    @EqualsAndHashCode(callSuper = false)
    public static class TestCommand extends Command<Void> {
        @Getter @Setter
        private boolean events;

        public TestCommand() {
        }

        public TestCommand(boolean events) {
            this.events = events;
        }

        @Override
        public Stream<Event> events(Repository repository) throws Exception {
            if (events) {
                return Stream.of(new TestEvent());
            } else {
                return super.events(repository);
            }
        }
    }

    @Test
    @SneakyThrows
    public void journalCounting() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        assertEquals(journal.journal((Command<?>) new TestCommand(true).timestamp(timestamp)), 1);
        timestamp.update();
        assertEquals(journal.journal((Command<?>) new TestCommand(false).timestamp(timestamp)), 0);
    }

    @Test
    @SneakyThrows
    public void journalListener() {
        AtomicInteger onEvent = new AtomicInteger(0);
        AtomicBoolean onCommit = new AtomicBoolean(false);
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        assertEquals(1, journal.journal((Command<?>) new TestCommand(true).timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                onEvent.incrementAndGet();
            }

            @Override
            public void onCommit() {
                onCommit.set(true);
            }
        }));

        assertEquals(onEvent.get(), 1);
        assertTrue(onCommit.get());
    }

    @Test
    @SneakyThrows
    public void journalRetrieving() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        List<Event> events = new ArrayList<>();
        TestCommand command = new TestCommand(true);
        journal.journal((Command<?>) command.timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });
        assertEquals(events.size(), 1);

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
        TestCommand command = new TestCommand(true);
        journal.journal((Command<?>) command.timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });

        Iterator<EntityHandle<TestCommand>> commandIterator = journal.commandIterator(TestCommand.class);
        assertTrue(commandIterator.hasNext());
        assertEquals(commandIterator.next().uuid(), command.uuid());
        assertFalse(commandIterator.hasNext());

        assertEquals(events.size(), 1);
        Event event = events.get(0);

        Iterator<EntityHandle<TestEvent>> eventIterator = journal.eventIterator(TestEvent.class);
        assertTrue(eventIterator.hasNext());
        assertEquals(eventIterator.next().uuid(), event.uuid());
        assertFalse(eventIterator.hasNext());
    }

}