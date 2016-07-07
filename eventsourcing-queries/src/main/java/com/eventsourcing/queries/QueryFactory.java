/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.EntityQueryFactory;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;

import java.util.function.Function;

public class QueryFactory implements EntityQueryFactory {
    /**
     * @see LatestAssociatedEntryQuery
     * @param collection collection to query against
     * @param query query as is
     * @param timestampAttribute timestamp attribute
     * @param <O> entity type
     * @return a query
     */
    public static <O extends Entity> Query<EntityHandle<O>>
    isLatestEntity(final IndexedCollection<EntityHandle<O>> collection,
                   final Query<EntityHandle<O>> query,
                   final com.googlecode.cqengine.attribute.Attribute<EntityHandle<O>, HybridTimestamp> timestampAttribute) {
        return new IsLatestEntity<>(collection, query, timestampAttribute);
    }

    /**
     * @see LatestAssociatedEntryQuery
     * @param collection collection to query against
     * @param query query returning function
     * @param timestampAttribute timestamp attribute
     * @param <O> entity type
     * @return a query
     */
    public static <O extends Entity> Query<EntityHandle<O>>
    isLatestEntity(final IndexedCollection<EntityHandle<O>> collection,
                   final Function<EntityHandle<O>, Query<EntityHandle<O>>> query,
                   final com.googlecode.cqengine.attribute.Attribute<EntityHandle<O>, HybridTimestamp> timestampAttribute) {
        return new IsLatestEntity<>(collection, query, timestampAttribute);
    }
}
