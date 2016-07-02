/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Entity;
import com.eventsourcing.EntitySubscriber;

/**
 * {@link EntitySubscriber} that subscribes to all entities that are inherited from
 * a certain class.
 * @param <T>
 */
public class ClassEntitySubscriber<T extends Entity> implements EntitySubscriber<T> {

    private final Class<T> klass;

    public ClassEntitySubscriber(Class<T> klass) {
        this.klass = klass;
    }

    @Override public boolean matches(T entity) {
        return klass.isAssignableFrom(entity.getClass());
    }

}
