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

/**
 * An extension of {@link com.googlecode.cqengine.attribute.SimpleAttribute} that hides
 * the unnecessary complexity of using {@link EntityHandle}
 *
 * @param <O>
 * @param <A>
 */
public abstract class SimpleAttribute<O extends Entity, A>
        extends com.googlecode.cqengine.attribute.SimpleAttribute<EntityHandle<O>, A>
        implements Attribute<O, A> {

    private Class<O> objectType;

    public SimpleAttribute() {
        super();
    }

    public SimpleAttribute(String attributeName) {
        super(attributeName);
    }

    public SimpleAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType) {
        super(handleType, attributeType);
        this.objectType = objectType;
    }

    public SimpleAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType, String attributeName) {
        super(handleType, attributeType, attributeName);
        this.objectType = objectType;
    }

    @Override
    public A getValue(EntityHandle<O> object, QueryOptions queryOptions) {
        return getValue(object.get(), queryOptions);
    }

    public abstract A getValue(O object, QueryOptions queryOptions);

    @Override public Class<O> getEffectiveObjectType() {
        return objectType == null ? Attribute.readGenericObjectType(getClass(), getAttributeName()) : objectType;
    }

}
