/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import java.util.stream.Stream;

/**
 * EntitySubscriber allows to listen to entities (commands and events)
 * once they are committed. Use {@link Repository#addEntitySubscriber(EntitySubscriber)}
 * to add an entity subscriber.
 *
 * <p/>
 *
 * When an event or a command are being processed, repository will
 * use {@link EntitySubscriber#matches(Entity)} to determine whether
 * the entity is being subscribed to. Please not that it will store
 * entity UUID until the entire command is committed, so if your command
 * generates an extremely high number of events, you might experience
 * significant memory usage penalty.
 * Once a command is processed and committed, {@link EntitySubscriber#accept(Stream)}
 * is invoked with a stream of entity handles.
 * By default, {@link EntitySubscriber#accept(Stream)} invokes {@link EntitySubscriber#onEntity(EntityHandle)}
 * for every entity handle.
 *
 * <p/>
 *
 * Most common entity subscriber is a {@link ClassEntitySubscriber}
 * @param <T>
 */
public interface EntitySubscriber<T extends Entity> {
    /**
     * Defines a predicate for matching entities
     * @param entity
     * @return true if the entity should be returned
     */
    default boolean matches(T entity) {
        return true;
    }

    /**
     * Used by {@link #accept(Stream)} to be invoked for every entity handle.
     * Does nothing by default.
     * @param entity
     */
    default void onEntity(EntityHandle<T> entity) {}

    /**
     * This method is invoked once the command is being committed and all relevant entities
     * have been collected. It provides a default implementation that invokes {@link #onEntity(EntityHandle)}
     * for every entity handle.
     * @param entityStream
     */
    default void accept(Stream<EntityHandle<T>> entityStream) {
        entityStream.forEach(this::onEntity);
    }
}
