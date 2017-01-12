/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.migrations;

import com.eventsourcing.*;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.LayoutName;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.eventsourcing.queries.QueryFactory.all;
import static com.eventsourcing.queries.QueryFactory.equal;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LayoutMigrationTest extends RepositoryTest {

    public LayoutMigrationTest() {
        super(LayoutMigrationTest.class.getPackage());
    }

    public static class TestCommand extends StandardCommand<Void, Void> {
        @Builder
        public TestCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override public EventStream<Void> events() throws Exception {
            return EventStream.of(TestEvent1.builder().x(0).build());
        }
    }

    @LayoutName("TestEvent")
    @Accessors(fluent = true)
    public static class TestEvent1 extends StandardEvent {
        @Getter
        private final int x;

        @Builder
        public TestEvent1(HybridTimestamp timestamp, int x) {
            super(timestamp);
            this.x = x;
        }
    }

    @LayoutName("TestEvent")
    @Accessors(fluent = true)
    public static class TestEvent2 extends StandardEvent {
        @Getter
        private final int y;

        @Builder
        public TestEvent2(HybridTimestamp timestamp, int y) {
            super(timestamp);
            this.y = y;
        }
    }

    public static class MigrationCommand extends StandardCommand<Void, Void> {

        @Builder
        public MigrationCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Override public EventStream<Void> events(Repository repository, LockProvider lockProvider) throws
                                                                                                       Exception {
            LayoutMigration<TestEvent1, TestEvent2> migration = new LayoutMigration<>(
                    TestEvent1.class, TestEvent2.class,
                    testEvent1 -> TestEvent2.builder().y(testEvent1.x() + 1).build());
            return EventStream.of(migration.events(repository, lockProvider));
        }
    }

    @Test @SneakyThrows
    public void test() {
        TestCommand command = TestCommand.builder().build();
        repository.publish(command).get();
        ResultSet<EntityHandle<TestEvent1>> resultSet1 = repository.query(TestEvent1.class, all(TestEvent1.class));
        assertEquals(resultSet1.size(), 1);
        TestEvent1 testEvent1 = resultSet1.uniqueResult().get();
        assertEquals(testEvent1.x, 0);

        ResultSet<EntityHandle<EventCausalityEstablished>> resultSetCausality1 = repository
                .query(EventCausalityEstablished.class, equal(EventCausalityEstablished.EVENT, testEvent1.uuid()));
        assertEquals(resultSetCausality1.size(), 1);
        assertEquals(resultSetCausality1.uniqueResult().get().command(), command.uuid());

        MigrationCommand migration = MigrationCommand.builder().build();
        repository.publish(migration).get();
        ResultSet<EntityHandle<TestEvent2>> resultSet2 = repository.query(TestEvent2.class, all(TestEvent2.class));
        assertEquals(resultSet2.size(), 1);
        TestEvent2 testEvent2 = resultSet2.uniqueResult().get();
        assertEquals(testEvent2.y, 1);

        ResultSet<EntityHandle<EventCausalityEstablished>> resultSetCausality2 = repository
                .query(EventCausalityEstablished.class, equal(EventCausalityEstablished.EVENT, testEvent2.uuid()));
        assertEquals(resultSetCausality2.size(), 2);
        List<EventCausalityEstablished> testEvent2Causality = Lists.newArrayList(resultSetCausality2.iterator())
                                                                   .stream()
                                                                   .map(EntityHandle::get).collect(Collectors.toList());

        assertTrue(testEvent2Causality.stream().anyMatch(c -> c.command().equals(command.uuid())));
        assertTrue(testEvent2Causality.stream().anyMatch(c -> c.command().equals(migration.uuid())));

    }
}