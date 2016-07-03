/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.Journal;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.UUID;

/**
 * Entity handle implementation that uses journal to access the entity
 * @param <T>
 */
public class JournalEntityHandle<T extends Entity> implements EntityHandle<T> {
    @Getter @Accessors(fluent = true)
    private final UUID uuid;
    private final Journal journal;

    public JournalEntityHandle(Journal journal, UUID uuid) {
        this.journal = journal;
        this.uuid = uuid;
    }

    @Override public Optional<T> getOptional() {
        return journal.get(uuid);
    }
}
