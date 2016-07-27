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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface Attribute<O extends Entity, A>
        extends com.googlecode.cqengine.attribute.Attribute<EntityHandle<O>, A> {

    Class<O> getEffectiveObjectType();

    default int calculateHashCode() {
        int result = getEffectiveObjectType().hashCode();
        result = 31 * result + getAttributeType().hashCode();
        result = 31 * result + getAttributeName().hashCode();
        return result;
    }


    default boolean isEqual(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;

        Attribute that = (Attribute) o;

        // TODO: reinstate this cachedHashCode comparison once EqualsVerifier supports cached hash code "shortcut":
        //if (cachedHashCode != that.cachedHashCode) return false;
        if (!that.canEqual(this)) return false;
        if (!getAttributeName().equals(that.getAttributeName())) return false;
        if (!getAttributeType().equals(that.getAttributeType())) return false;
        if (!getEffectiveObjectType().equals(that.getEffectiveObjectType())) return false;

        return true;
    }

    default boolean canEqual(Object other) {
        return other instanceof Attribute;
    }


    static <O> Class<O> readGenericObjectType(Class<?> attributeClass, String attributeName) {
        try {
            ParameterizedType superclass = (ParameterizedType) attributeClass.getGenericSuperclass();
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            Type actualType = superclass.getActualTypeArguments()[0];
            Class<O> cls;
            if (actualType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) actualType;
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                Class<O> actualClass = (Class<O>) parameterizedType.getRawType();
                cls = actualClass;
            } else {
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                Class<O> actualClass = (Class<O>) actualType;
                cls = actualClass;
            }
            return cls;
        } catch (Exception e) {
            String attributeClassStr = attributeName
                    .startsWith("<Unnamed attribute, class ") ? "" : " (" + attributeClass + ")";
            throw new IllegalStateException(
                    "Attribute '" + attributeName + "'" + attributeClassStr + " is invalid, cannot read generic type information from it. Attributes should typically EITHER be declared in code with generic type information as a (possibly anonymous) subclass of one of the provided attribute types, OR you can use a constructor of the attribute which allows the types to be specified manually.");
        }
    }

}
