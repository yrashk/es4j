/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Collections;

/**
 * Designates a simple (single value) index
 * @param <O> entity type
 * @param <A> attribute type
 */
@FunctionalInterface
public interface SimpleIndex<O extends Entity, A> extends EntityIndex<O, A> {
    A getValue(O object, QueryOptions queryOptions);

    default Iterable<A> getValues(O object, QueryOptions queryOptions) {
        return Collections.singletonList(getValue(object, queryOptions));
    }

    default Attribute<O, A> getAttribute() {
        throw new IllegalStateException("Index " + this + " hasn't been initialized yet");
    }

}
