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
package org.eventchain.index;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.*;
import lombok.experimental.Accessors;
import org.eventchain.*;
import org.eventchain.hlc.NTPServerTimeProvider;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static org.eventchain.index.IndexEngine.IndexFeature.EQ;
import static org.eventchain.index.IndexEngine.IndexFeature.SC;
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


    @Value @EqualsAndHashCode(callSuper=false)
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
        Index<EntityHandle<TestEvent>> index = indexEngine.getIndexOnAttribute(TestEvent.ATTR, EQ, SC);
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
        coll.add(new EntityHandle<>(journal, event.uuid()));

        EntityHandle<TestEvent> handle = coll.retrieve(equal(TestEvent.ATTR, "test")).uniqueResult();
        assertTrue(handle.get().isPresent());

        handle = coll.retrieve(contains(TestEvent.ATTR, "es")).uniqueResult();
        assertTrue(handle.get().isPresent());

        handle = coll.retrieve(not(contains(TestEvent.ATTR, "se"))).uniqueResult();
        assertTrue(handle.get().isPresent());
    }
}