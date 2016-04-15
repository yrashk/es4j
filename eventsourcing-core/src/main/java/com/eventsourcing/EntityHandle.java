/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package com.eventsourcing;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.UUID;

/**
 * EntityHandle is a "lazy loading" handle for {@link Entity}
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
     * @return
     */
    public Optional<T> getOptional() {
        return journal.get(uuid);
    }

    /**
     * Returns the referenced entity
     * @return
     * @throws java.util.NoSuchElementException if the entity wasn't found
     */
    public T get() {
        return getOptional().get();
    }
}
