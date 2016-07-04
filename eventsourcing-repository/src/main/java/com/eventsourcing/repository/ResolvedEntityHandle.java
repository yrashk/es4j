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

import java.util.Optional;
import java.util.UUID;

public class ResolvedEntityHandle<T extends Entity> implements EntityHandle<T> {

    private final T entity;

    public ResolvedEntityHandle(T entity) {
        this.entity = entity;
    }

    @Override public Optional<T> getOptional() {
        return Optional.of(entity);
    }

    @Override public T get() {
        return entity;
    }

    @Override public UUID uuid() {
        return entity.uuid();
    }
}
