/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.*;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.SimpleAttribute;
import com.google.common.collect.Iterators;
import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;
import static com.eventsourcing.queries.QueryFactory.isLatestEntity;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class IsLatestEntityTest extends RepositoryUsingTest {

    public IsLatestEntityTest() {
        super(IsLatestEntityTest.class.getPackage());
    }


    @Value
    @EqualsAndHashCode(callSuper = false)
    @Accessors(fluent = true)
    public static class TestEvent extends StandardEvent {
        private String test;
        private UUID reference;
        @Index
        public static SimpleAttribute<TestEvent, UUID> REFERENCE_ID = new
                SimpleAttribute<TestEvent, UUID>("uuid") {
            @Override public UUID getValue(TestEvent object, QueryOptions queryOptions) {
                return object.reference();
            }
        };
        @Index({EQ, LT, GT})
        public static SimpleAttribute<TestEvent, HybridTimestamp> TIMESTAMP = new
                SimpleAttribute<TestEvent, HybridTimestamp>("timestamp") {
                    @Override public HybridTimestamp getValue(TestEvent object, QueryOptions queryOptions) {
                        return object.timestamp();
                    }
        };
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @Accessors(fluent = true)
    public static class TestCommand extends StandardCommand<Void, UUID> {
        private String test;
        private UUID reference;

        @Override public EventStream<Void> events(Repository repository) throws Exception {
            TestEvent event = new TestEvent(test, reference);
            return EventStream.of(event);
        }

        @Override public UUID result() {
            return reference;
        }
    }

    @Test @SneakyThrows
    public void test() {
        UUID uuid = UUID.randomUUID();
        repository.publish(new TestCommand("test1", uuid)).get();
        IndexedCollection<EntityHandle<TestEvent>> coll = repository.getIndexEngine().getIndexedCollection
                (TestEvent.class);
        Query<EntityHandle<TestEvent>> query = isLatestEntity(coll, equal(TestEvent.REFERENCE_ID, uuid),
                                                              TestEvent.TIMESTAMP);
        try (ResultSet<EntityHandle<TestEvent>> resultSet = repository.query(TestEvent.class, query)) {
            assertEquals(resultSet.size(), 1);
            assertEquals(resultSet.uniqueResult().get().test(), "test1");
        }
        repository.publish(new TestCommand("test2", uuid)).get();
        try (ResultSet<EntityHandle<TestEvent>> resultSet = repository.query(TestEvent.class, query)) {
            assertEquals(resultSet.size(), 1);
            assertEquals(resultSet.uniqueResult().get().test(), "test2");
        }
    }

    @Test @SneakyThrows
    public void testMassive() {
        UUID uuid = UUID.randomUUID();
        for (int i = 0; i < 10000; i++ ) {
            repository.publish(new TestCommand("test" + (i + 1), uuid)).get();
        }
        IndexedCollection<EntityHandle<TestEvent>> coll = repository.getIndexEngine().getIndexedCollection
                (TestEvent.class);

        Query<EntityHandle<TestEvent>> query = isLatestEntity(coll, equal(TestEvent.REFERENCE_ID, uuid),
                                                              TestEvent.TIMESTAMP);
        long t1 = System.nanoTime();
        try (ResultSet<EntityHandle<TestEvent>> resultSet = repository.query(TestEvent.class, query)) {
            assertEquals(resultSet.size(), 1);
            assertEquals(resultSet.uniqueResult().get().test(), "test10000");
            long t2 = System.nanoTime();
            long time = TimeUnit.SECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS);
            if (time > 1) {
                System.err.println("Warning: [IsLatestEntityTest.testMassive] isLatestEntity is slow, took " + time +
                                           " seconds");
            }
        }
    }

    @Test @SneakyThrows
    public void testFunction() {
        UUID uuid = UUID.randomUUID();
        repository.publish(new TestCommand("test1", uuid)).get();
        repository.publish(new TestCommand("test2", uuid)).get();
        UUID uuidN = UUID.randomUUID();
        repository.publish(new TestCommand("testN1", uuidN)).get();
        repository.publish(new TestCommand("testN2", uuidN)).get();

        IndexedCollection<EntityHandle<TestEvent>> coll = repository.getIndexEngine().getIndexedCollection
                (TestEvent.class);
        Query<EntityHandle<TestEvent>> query = isLatestEntity(coll,
                                                              (h) -> equal(TestEvent.REFERENCE_ID, h.get().reference()),
                                                              TestEvent.TIMESTAMP);
        try (ResultSet<EntityHandle<TestEvent>> resultSet = repository.query(TestEvent.class, query)) {
            assertEquals(resultSet.size(), 2);
            List<EntityHandle<TestEvent>> result = Iterables.toList(resultSet);
            assertTrue(Iterators.any(result.iterator(), e -> e.get().test().contentEquals("test2")));
            assertTrue(Iterators.any(result.iterator(), e -> e.get().test().contentEquals("testN2")));
        }
    }
}