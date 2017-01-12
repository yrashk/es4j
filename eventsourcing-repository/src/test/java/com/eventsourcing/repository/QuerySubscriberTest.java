/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.*;
import com.eventsourcing.index.JavaStaticFieldIndexLoader;
import com.eventsourcing.index.SimpleIndex;
import com.eventsourcing.repository.QuerySubscriber;
import com.googlecode.cqengine.query.Query;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.eventsourcing.queries.QueryFactory.and;
import static com.eventsourcing.queries.QueryFactory.equal;
import static com.eventsourcing.queries.QueryFactory.existsIn;
import static org.testng.Assert.*;

public class QuerySubscriberTest extends RepositoryUsingTest {

    public QuerySubscriberTest() {
        super(QuerySubscriber.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class TestEvent extends StandardEvent {

        @Getter
        private String test;

        public TestEvent(String test) {
            this.test = test;
        }

        public static SimpleIndex<TestEvent, String> TEST = TestEvent::test;
    }

    @Accessors(fluent = true)
    public static class AnotherEvent extends StandardEvent {

        @Getter
        private String test;

        public AnotherEvent(String test) {
            this.test = test;
        }

        public static SimpleIndex<AnotherEvent, String> TEST = AnotherEvent::test;
    }

    public static class TestCommand extends StandardCommand<Void, Void> {
        @Override public EventStream<Void> events() throws Exception {
            TestEvent event = new TestEvent("test");
            return EventStream.ofWithState(null, event);
        }
    }

    public static class AnotherCommand extends StandardCommand<Void, Void> {
        @Override public EventStream<Void> events() throws Exception {
            AnotherEvent event = new AnotherEvent("test");
            return EventStream.ofWithState(null, event);
        }
    }

    @Test(timeOut = 2000)
    @SneakyThrows
    public void test() {
        Query<EntityHandle<TestEvent>> testQuery = equal(TestEvent.TEST, "test");
        QuerySubscriber querySubscriber = new QuerySubscriber(repository);
        querySubscriber.bindIndexLoader(new JavaStaticFieldIndexLoader());

        CompletableFuture<Void> future = new CompletableFuture<>();
        querySubscriber.addQuery(TestEvent.class, testQuery, (q) -> future.complete(null));

        repository.addEntitySubscriber(querySubscriber);
        repository.publish(new TestCommand()).get();

        future.get();
    }

    @Test(timeOut = 2000)
    @SneakyThrows
    public void testJoin() {
        Query<EntityHandle<TestEvent>> testQuery = and(equal(TestEvent.TEST, "test"),
                                                       existsIn(repository.getIndexEngine().getIndexedCollection
                                                               (AnotherEvent.class),
                                                                TestEvent.TEST, AnotherEvent.TEST));
        QuerySubscriber querySubscriber = new QuerySubscriber(repository);
        querySubscriber.bindIndexLoader(new JavaStaticFieldIndexLoader());

        CompletableFuture<Void> future = new CompletableFuture<>();
        querySubscriber.addQuery(TestEvent.class, testQuery, (q) -> future.complete(null));

        repository.addEntitySubscriber(querySubscriber);

        repository.publish(new AnotherCommand()).get();
        repository.publish(new TestCommand()).get();

        future.get();
    }

    @Test
    @SneakyThrows
    public void testNegative() {
        Query<EntityHandle<TestEvent>> testQuery = equal(TestEvent.TEST, "test1");
        QuerySubscriber querySubscriber = new QuerySubscriber(repository);
        querySubscriber.bindIndexLoader(new JavaStaticFieldIndexLoader());

        CompletableFuture<Void> future = new CompletableFuture<>();
        querySubscriber.addQuery(TestEvent.class, testQuery, (q) -> future.complete(null));

        repository.addEntitySubscriber(querySubscriber);
        repository.publish(new TestCommand()).get();

        try {
            future.get(1, TimeUnit.SECONDS);
            assertFalse(true, "should not receive a result");
        } catch (TimeoutException e) {
        }
    }
}