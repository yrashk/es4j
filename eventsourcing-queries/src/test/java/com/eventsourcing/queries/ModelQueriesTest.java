/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.StandardEvent;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.UUID;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ModelQueriesTest extends RepositoryUsingTest {

    public ModelQueriesTest() {
        super(ModelQueriesTest.class.getPackage());
    }

    public static class TestEvent extends StandardEvent {
        public static SimpleAttribute<TestEvent, UUID> ID = new SimpleAttribute<TestEvent, UUID>("uuid") {
            @Override public UUID getValue(TestEvent object, QueryOptions queryOptions) {
                return object.uuid();
            }
        };
    }

    public static class TestCommand extends StandardCommand<TestEvent, UUID> {
        @Override public EventStream<TestEvent> events() throws Exception {
            TestEvent event = new TestEvent();
            return EventStream.ofWithState(event, event);
        }

        @Override public UUID result(TestEvent state) {
            return state.uuid();
        }
    }

    @Test
    @SneakyThrows
    public void lookup() {
        UUID uuid = repository.publish(new TestCommand()).get();
        Optional<TestEvent> correct = ModelQueries.lookup(repository, TestEvent.class, TestEvent.ID, uuid);
        assertTrue(correct.isPresent());
        Optional<TestEvent> incorrect = ModelQueries.lookup(repository, TestEvent.class, TestEvent.ID, UUID.randomUUID());
        assertFalse(incorrect.isPresent());

    }
}