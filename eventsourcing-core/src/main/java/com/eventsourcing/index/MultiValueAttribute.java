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

import java.lang.reflect.Type;

public abstract class MultiValueAttribute<O extends Entity, A>
        extends com.googlecode.cqengine.attribute.MultiValueAttribute<EntityHandle<O>, A>
        implements Attribute<O, A>, ReflectableAttribute<A> {

    private Class<O> objectType;
    private int cachedHashCode;

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override public Class<O> getEffectiveObjectType() {
        return objectType == null ? Attribute.readGenericObjectType(getClass(), getAttributeName()) : objectType;
    }

    public MultiValueAttribute() {
        super();
        cachedHashCode = calculateHashCode();
    }

    public MultiValueAttribute(String attributeName) {
        super(attributeName);
        cachedHashCode = calculateHashCode();
    }

    public MultiValueAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType) {
        super(handleType, attributeType);
        this.objectType = objectType;
        cachedHashCode = calculateHashCode();
    }

    public MultiValueAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType,
                               String attributeName) {
        super(handleType, attributeType, attributeName);
        this.objectType = objectType;
        cachedHashCode = calculateHashCode();
    }

    @Override public boolean equals(Object o) {
        return isEqual(o);
    }

    @Override
    public Iterable<A> getValues(EntityHandle<O> object, QueryOptions queryOptions) {
        return getValues(object.get(), queryOptions);
    }

    public abstract Iterable<A> getValues(O object, QueryOptions queryOptions);

    @Override public boolean canEqual(Object other) {
        return other instanceof MultiValueAttribute;
    }

    @Override public String toString() {
        return "MultiValueAttribute{" +
                "objectType=" + getEffectiveObjectType() +
                ", attributeType=" + getAttributeType() +
                ", attributeName='" + getAttributeName() + '\'' +
                '}';
    }
}