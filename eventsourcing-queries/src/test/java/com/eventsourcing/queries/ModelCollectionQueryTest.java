/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.Model;
import com.eventsourcing.Repository;
import com.google.common.collect.Iterables;
import lombok.Value;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import static com.eventsourcing.queries.ModelCollectionQuery.LogicalOperators.and;
import static com.eventsourcing.queries.ModelCollectionQuery.LogicalOperators.or;
import static org.testng.Assert.*;

public class ModelCollectionQueryTest {

    @Value
    static class MyModel implements Model {
        private Repository repository;
        private UUID id;
        @Accessors(fluent = true)
        private String name;
    }

    static class MyQuery1 implements ModelCollectionQuery<MyModel> {

        @Override public Stream<MyModel> getCollectionStream(Repository repository) {
            return Stream.of(new MyModel(repository, UUID.nameUUIDFromBytes("test1".getBytes()), "test1"),
                             new MyModel(repository, UUID.nameUUIDFromBytes("test1_1".getBytes()), "test1_1"));
        }
    }

    static class MyQuery2 implements ModelCollectionQuery<MyModel> {

        @Override public Stream<MyModel> getCollectionStream(Repository repository) {
            return Stream.of(new MyModel(repository, UUID.nameUUIDFromBytes("test2".getBytes()), "test2"),
                             new MyModel(repository, UUID.nameUUIDFromBytes("test1_1".getBytes()), "test1_1"));
        }
    }

    @Test
    public void query() {
        Collection<MyModel> collection = ModelCollectionQuery.query(null, new MyQuery1());
        Iterables.any(collection, m -> m.name().contains("test1"));
        Iterables.any(collection, m -> m.name().contains("test1_1"));
    }

    @Test
    public void queryAnd() {
        Collection<MyModel> collection = ModelCollectionQuery.query(null, and(new MyQuery1(), new MyQuery2()));
        assertEquals(collection.size(), 1);
        Iterables.any(collection, m -> m.name().contains("test1_1"));
    }

    @Test
    public void queryOr() {
        Collection<MyModel> collection = ModelCollectionQuery.query(null, or(new MyQuery1(), new MyQuery2()));
        assertEquals(collection.size(), 3);
        Iterables.any(collection, m -> m.name().contains("test1"));
        Iterables.any(collection, m -> m.name().contains("test1_1"));
        Iterables.any(collection, m -> m.name().contains("test2"));
    }

}