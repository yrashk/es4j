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
import com.eventsourcing.Repository;
import com.eventsourcing.index.EntityIndex;
import com.googlecode.cqengine.resultset.ResultSet;

import java.util.Optional;
import java.util.UUID;

import static com.eventsourcing.queries.QueryFactory.equal;

/**
 * Combines all standard queries into one:
 * <ul>
 *     <li>{@link LatestAssociatedEntryQuery}</li>
 * </ul>
 */
public interface ModelQueries extends Model, LatestAssociatedEntryQuery {

    /**
     * Lookup an entity by a unique ID.
     *
     * @param repository repository
     * @param klass entity klass
     * @param keyAttribute entity ID attribute
     * @param id ID
     * @param <T> entity type
     * @return Non-empty {@link Optional} if the entity is found, empty otherwise
     */
    static <T extends Entity> Optional<T>
            lookup(Repository repository, Class<T> klass, EntityIndex<T, UUID> keyAttribute, UUID id) {
        try (ResultSet<EntityHandle<T>> resultSet = repository.query(klass, equal(keyAttribute, id))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(resultSet.uniqueResult().get());
            }
        }
    }

}
