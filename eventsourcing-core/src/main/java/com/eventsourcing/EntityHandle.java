/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import java.util.Optional;

/**
 * EntityHandle is a "lazy loading" handle for {@link Entity}
 *
 * @param <T>
 */
public interface EntityHandle<T extends Entity> {
    /**
     * Returns an optional value of the referenced entity (empty if an entity specified by a given
     * UUID can't be found). When the entity is expected to be found, {@link #get()}
     * should be used instead
     *
     * @return
     */
    Optional<T> getOptional();

    /**
     * Returns the referenced entity
     *
     * @return
     * @throws java.util.NoSuchElementException if the entity wasn't found
     */
    default T get() {
        return getOptional().get();
    }

    java.util.UUID uuid();
}
