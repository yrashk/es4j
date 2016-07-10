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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;

/**
 * An extension of {@link com.googlecode.cqengine.attribute.SimpleAttribute} that hides
 * the unnecessary complexity of using {@link EntityHandle}
 *
 * @param <O>
 * @param <A>
 */
public abstract class SimpleAttribute<O extends Entity, A> extends AbstractAttribute<O, A> {

    public SimpleAttribute() {
        super();
    }

    public SimpleAttribute(String attributeName) {
        super(attributeName);
    }

    public SimpleAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType) {
        super(objectType, handleType, attributeType);
    }

    public SimpleAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType,
                           String attributeName) {
        super(objectType, handleType, attributeType, attributeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<A> getValues(EntityHandle<O> object, QueryOptions queryOptions) {
        return Collections.singletonList(getValue(object, queryOptions));
    }

    public A getValue(EntityHandle<O> object, QueryOptions queryOptions) {
        return getValue(object.get(), queryOptions);
    }

    public abstract A getValue(O object, QueryOptions queryOptions);

    @Override public boolean canEqual(Object other) {
        return other instanceof SimpleAttribute;
    }
}
