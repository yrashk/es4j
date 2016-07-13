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

/**
 * Designates a multiple values index
 * @param <O> entity type
 * @param <A> attribute type
 */
@FunctionalInterface
public interface MultiValueIndex<O extends Entity, A> extends EntityIndex<O, A> {
    Iterable<A> getValues(O object, QueryOptions queryOptions);

    default Attribute<O, A> getAttribute() {
        throw new IllegalStateException("Index " + this + " hasn't been initialized yet");
    }
}
