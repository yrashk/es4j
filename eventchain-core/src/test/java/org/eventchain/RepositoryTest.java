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

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.eventchain.annotations.Index;
import org.eventchain.hlc.NTPServerTimeProvider;
import org.eventchain.index.MemoryIndexEngine;
import org.eventchain.index.SimpleAttribute;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.eventchain.index.IndexEngine.IndexFeature.EQ;
import static org.eventchain.index.IndexEngine.IndexFeature.SC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static com.googlecode.cqengine.query.QueryFactory.*;

public abstract class RepositoryTest<T extends Repository> {

    private final T repository;
    private Journal journal;
    private MemoryIndexEngine indexEngine;

    public RepositoryTest(T repository) {
        this.repository = repository;
    }

    @BeforeClass
    public void setUpEnv() throws Exception {
        journal = new MemoryJournal();
        repository.setJournal(journal);
        repository.setPackage(getClass().getPackage());
        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider();
        timeProvider.startAsync().awaitRunning();
        repository.setPhysicalTimeProvider(timeProvider);
        indexEngine = new MemoryIndexEngine();
        repository.setIndexEngine(indexEngine);
        indexEngine.setRepository(repository);
        indexEngine.setJournal(journal);
        repository.startAsync().awaitRunning();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        journal.clear();
    }

    @Accessors(fluent = true) @ToString
    public static class TestEvent extends Event {
        @Getter @Setter
        private String string;

        @Index({EQ, SC})
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
    public void indexing() {
        repository.publish(new RepositoryTestCommand()).get();
        IndexedCollection<EntityHandle<TestEvent>> coll = indexEngine.getIndexedCollection(TestEvent.class);
        assertTrue(coll.retrieve(equal(TestEvent.ATTR, "test")).isNotEmpty());
        assertTrue(coll.retrieve(contains(TestEvent.ATTR, "es")).isNotEmpty());
    }

}