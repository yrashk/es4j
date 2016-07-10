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

public abstract class AbstractAttribute<O extends Entity, A>
        extends com.googlecode.cqengine.attribute.support.AbstractAttribute<EntityHandle<O>, A>
        implements Attribute<O, A> {

    private Class<O> objectType;
    private int cachedHashCode;

    public AbstractAttribute() {
        super();
    }

    public AbstractAttribute(String attributeName) {
        super(attributeName);
    }

    public AbstractAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType) {
        super(handleType, attributeType);
        this.objectType = objectType;
        cachedHashCode = calcHashCode();
    }

    public AbstractAttribute(Class<O> objectType, Class<EntityHandle<O>> handleType, Class<A> attributeType, String attributeName) {
        super(handleType, attributeType, attributeName);
        this.objectType = objectType;
    }

    @Override public Class<O> getEffectiveObjectType() {
        return objectType == null ? Attribute.readGenericObjectType(getClass(), getAttributeName()) : objectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractAttribute)) return false;

        AbstractAttribute that = (AbstractAttribute) o;

        // TODO: reinstate this cachedHashCode comparison once EqualsVerifier supports cached hash code "shortcut":
        //if (cachedHashCode != that.cachedHashCode) return false;
        if (!that.canEqual(this)) return false;
        if (!getAttributeName().equals(that.getAttributeName())) return false;
        if (!getAttributeType().equals(that.getAttributeType())) return false;
        if (!objectType.equals(that.objectType)) return false;

        return true;
    }

    public boolean canEqual(Object other) {
        return other instanceof AbstractAttribute;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    int calcHashCode() {
        int result = objectType.hashCode();
        result = 31 * result + getAttributeType().hashCode();
        result = 31 * result + getAttributeName().hashCode();
        return result;
    }


}
