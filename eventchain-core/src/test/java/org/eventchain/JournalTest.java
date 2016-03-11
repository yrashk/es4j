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
import lombok.SneakyThrows;
import lombok.Value;
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

    private final T journal;
    private final RepositoryImpl repository;
    private final IndexEngine indexEngine;

    @SneakyThrows
    public JournalTest(T journal) {
        this.journal = journal;
        repository = new RepositoryImpl();
        repository.setPackage(getClass().getPackage());
        repository.setJournal(this.journal);
        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider();
        repository.setPhysicalTimeProvider(timeProvider);
        repository.setLockProvider(new MemoryLockProvider());
        indexEngine = new MemoryIndexEngine();
        indexEngine.setJournal(journal);
        indexEngine.setRepository(repository);
        repository.setIndexEngine(indexEngine);
        this.journal.setRepository(repository);
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        repository.startAsync().awaitRunning();
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        repository.stopAsync().awaitTerminated();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        journal.clear();
    }

    static class TestEvent extends Event {}

    @Value @EqualsAndHashCode(callSuper = false)
    static class TestCommand extends Command<Void> {
        private boolean events;

        @Override
        public Stream<Event> events(Repository repository) {
            if (events) {
                return Stream.of(new TestEvent());
            } else {
                return super.events(repository);
            }
        }
    }

    @Test
    public void journalCounting() {
        assertEquals(1, journal.journal(new TestCommand(true)));
        assertEquals(0, journal.journal(new TestCommand(false)));
    }

    @Test
    public void journalListener() {
        AtomicInteger onEvent = new AtomicInteger(0);
        AtomicBoolean onCommit = new AtomicBoolean(false);

        assertEquals(1, journal.journal(new TestCommand(true), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                onEvent.incrementAndGet();
            }

            @Override
            public void onCommit() {
                onCommit.set(true);
            }
        }));

        assertEquals(1, onEvent.get());
        assertTrue(onCommit.get());
    }

    @Test
    public void journalRetrieving() {
        List<Event> events = new ArrayList<>();
        TestCommand command = new TestCommand(true);
        journal.journal(command, new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });
        assertEquals(1, events.size());

        Optional<Entity> entity = journal.get(command.uuid());
        assertTrue(entity.isPresent());
        assertEquals(command, entity.get());

        Event event = events.get(0);
        Optional<Entity> eventEntity = journal.get(event.uuid());
        assertTrue(eventEntity.isPresent());
        assertEquals(event, eventEntity.get());
    }

    @Test
    public void journalIterating() {
        List<Event> events = new ArrayList<>();
        TestCommand command = new TestCommand(true);
        journal.journal(command, new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });

        Iterator<EntityHandle<TestCommand>> commandIterator = journal.commandIterator(TestCommand.class);
        assertTrue(commandIterator.hasNext());
        assertEquals(commandIterator.next().uuid(), command.uuid());
        assertFalse(commandIterator.hasNext());

        assertEquals(1, events.size());
        Event event = events.get(0);

        Iterator<EntityHandle<TestEvent>> eventIterator = journal.eventIterator(TestEvent.class);
        assertTrue(eventIterator.hasNext());
        assertEquals(eventIterator.next().uuid(), event.uuid());
        assertFalse(eventIterator.hasNext());
    }

}