/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.*;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.*;
import lombok.experimental.Accessors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static org.testng.Assert.assertTrue;

public abstract class IndexEngineTest<T extends IndexEngine> {

    private final T indexEngine;
    private final Repository repository;
    private final Journal journal;
    private final NTPServerTimeProvider timeProvider;

    @SneakyThrows
    public IndexEngineTest(T indexEngine) {
        this.indexEngine = indexEngine;

        repository = new RepositoryImpl();
        journal = new MemoryJournal();
        journal.setRepository(repository);
        repository.setJournal(journal);
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{getClass().getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{getClass().getPackage()}));
        repository.setIndexEngine(indexEngine);
        repository.setLockProvider(new MemoryLockProvider());
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);

        indexEngine.setJournal(journal);
        indexEngine.setRepository(repository);
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


    @Value @EqualsAndHashCode(callSuper = false)
    static class TestEvent extends Event {
        private String string;
        public static SimpleAttribute<TestEvent, String> ATTR = new SimpleAttribute<TestEvent, String>() {
            @Override
            public String getValue(TestEvent object, QueryOptions queryOptions) {
                return object.getString();
            }
        };
    }

    @Accessors(fluent = true)
    static class TestCommand extends Command<Void> {
        @Getter @Setter
        private String string;

        @Override
        public Stream<Event> events(Repository repository) {
            return Stream.of(new TestEvent(string));
        }
    }

    @Test
    @SneakyThrows
    public void test() {
        Index<EntityHandle<TestEvent>> index = indexEngine
                .getIndexOnAttribute(TestEvent.ATTR, IndexEngine.IndexFeature.EQ, IndexEngine.IndexFeature.SC);
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        List<Event> events = new ArrayList<>();
        TestCommand command = new TestCommand().string("test");
        journal.journal(command, new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });
        Event event = events.get(0);
        coll.add(new JournalEntityHandle<>(journal, event.uuid()));

        EntityHandle<TestEvent> handle = coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult();
        assertTrue(handle.getOptional().isPresent());

        handle = coll.retrieve(contains(TestEvent.ATTR, "es")).uniqueResult();
        assertTrue(handle.getOptional().isPresent());

        handle = coll.retrieve(not(contains(TestEvent.ATTR, "se"))).uniqueResult();
        assertTrue(handle.getOptional().isPresent());
    }
}