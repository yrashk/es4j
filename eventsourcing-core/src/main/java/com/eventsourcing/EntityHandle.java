/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.UUID;

/**
 * EntityHandle is a "lazy loading" handle for {@link Entity}
 *
 * @param <T>
 */
public class EntityHandle<T extends Entity> {
    @Getter @Accessors(fluent = true)
    private final UUID uuid;
    private final Journal journal;

    public EntityHandle(Journal journal, UUID uuid) {
        this.journal = journal;
        this.uuid = uuid;
    }

    /**
     * Returns an optional value of the referenced entity (empty if an entity specified by a given
     * UUID can't be found). When the entity is expected to be found, {@link #get()}
     * should be used instead
     *
     * @return
     */
    public Optional<T> getOptional() {
        return journal.get(uuid);
    }

    /**
     * Returns the referenced entity
     *
     * @return
     * @throws java.util.NoSuchElementException if the entity wasn't found
     */
    public T get() {
        return getOptional().get();
    }
}
