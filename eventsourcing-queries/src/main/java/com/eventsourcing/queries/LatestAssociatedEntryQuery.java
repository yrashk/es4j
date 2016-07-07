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
import com.eventsourcing.Model;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.EngineThresholds;
import com.googlecode.cqengine.resultset.ResultSet;

import java.util.Optional;
import java.util.UUID;

import static com.googlecode.cqengine.query.QueryFactory.*;

/**
 * Provides a query for retrieving the latest entry, associated with a model. For example,
 * to retrieve the latest e-mail change:
 *
 * <code>
 *     latestAssociatedEntity(EmailChanged.class, EmailChanged.REFERENCE_ID, EmailChanged.TIMESTAMP)
 * </code>
 */
public interface LatestAssociatedEntryQuery extends Model {

    /**
     * Invokes {@link #latestAssociatedEntity(Class, Attribute, Attribute, Query[])} with no additional queries
     *
     * @param klass Entity class
     * @param keyAttribute Entity attribute that references model's ID
     * @param timestampAttribute Entity attribute that holds the timestamp
     * @param <T> Entity type
     * @return Non-empty {@link Optional} if the entity is found, an empty one otherwise.
     */
    default <T extends Entity> Optional<T> latestAssociatedEntity(Class<T> klass,
                                                                  Attribute<T, UUID> keyAttribute,
                                                                  Attribute<T, HybridTimestamp> timestampAttribute) {
        @SuppressWarnings("unchecked")
        Optional<T> last = latestAssociatedEntity(klass, keyAttribute, timestampAttribute, (Query<EntityHandle<T>>[]) new Query[]{});
        return last;
    }

    /**
     * Invokes {@link #latestAssociatedEntity(Class, Attribute, Attribute, Query[])} with one additional query
     * @param klass Entity class
     * @param keyAttribute Entity attribute that references model's ID
     * @param timestampAttribute Entity attribute that holds the timestamp
     * @param additionalQuery An additional condition
     * @param <T> Entity type
     * @return Non-empty {@link Optional} if the entity is found, an empty one otherwise.
     */
    default <T extends Entity> Optional<T> latestAssociatedEntity(Class<T> klass,
                                                                  Attribute<T, UUID> keyAttribute,
                                                                  Attribute<T, HybridTimestamp> timestampAttribute,
                                                                  Query<EntityHandle<T>> additionalQuery
    ) {
        @SuppressWarnings("unchecked")
        Optional<T> last = latestAssociatedEntity(klass, keyAttribute, timestampAttribute, (Query<EntityHandle<T>>[]) new Query[]{additionalQuery});
        return last;
    }

    /**
     * Queries the latest entity associated with the model. For example,
     * to retrieve the latest e-mail change:
     *
     * <code>
     *     latestAssociatedEntity(EmailChanged.class, EmailChanged.REFERENCE_ID, EmailChanged.TIMESTAMP)
     * </code>
     *
     * If additional conditions are required, they can be added to the end of the method call:
     *
     * <code>
     *     latestAssociatedEntity(EmailChanged.class, EmailChanged.REFERENCE_ID, EmailChanged.TIMESTAMP,
     *                            equal(EmailChanged.APPROVED, true))
     * </code>
     *
     * @param klass Entity class
     * @param keyAttribute Entity attribute that references model's ID
     * @param timestampAttribute Entity attribute that holds the timestamp
     * @param additionalQueries Additional conditions
     * @param <T> Entity type
     * @return Non-empty {@link Optional} if the entity is found, an empty one otherwise.
     */
    default <T extends Entity> Optional<T>
    latestAssociatedEntity(Class<T> klass,
                           Attribute<T, UUID> keyAttribute, Attribute<T, HybridTimestamp> timestampAttribute,
                           Query<EntityHandle<T>> ...additionalQueries) {
        Query<EntityHandle<T>> query = equal(keyAttribute, getId());
        for (Query<EntityHandle<T>> q : additionalQueries) {
            query = and(query, q);
        }
        try (ResultSet<EntityHandle<T>> resultSet = getRepository()
                .query(klass, query,
                       queryOptions(orderBy(descending(timestampAttribute)),
                                    applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(resultSet.iterator().next().get());
            }
        }
    }

}
