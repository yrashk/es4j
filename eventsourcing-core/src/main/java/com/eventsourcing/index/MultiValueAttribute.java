/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.googlecode.cqengine.query.option.QueryOptions;

public abstract class MultiValueAttribute<O extends Entity, A> extends AbstractAttribute<O, A> {

    public MultiValueAttribute() {
        super();
    }

    public MultiValueAttribute(String attributeName) {
        super(attributeName);
    }

    public MultiValueAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType) {
        super(objectType, handleType, attributeType);
    }

    public MultiValueAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType,
                               String attributeName) {
        super(objectType, handleType, attributeType, attributeName);
    }

    @Override
    public Iterable<A> getValues(EntityHandle<O> object, QueryOptions queryOptions) {
        return getValues(object.get(), queryOptions);
    }

    public abstract Iterable<A> getValues(O object, QueryOptions queryOptions);

    @Override public boolean canEqual(Object other) {
        return other instanceof MultiValueAttribute;
    }
}