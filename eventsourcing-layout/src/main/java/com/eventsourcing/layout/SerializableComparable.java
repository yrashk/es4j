/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface SerializableComparable<T> {
    T getSerializableComparable();
    static Class<?> getType(Class<?> t) {
        Type[] interfaces = t.getGenericInterfaces();
        for (Type iface : interfaces) {
            ParameterizedType type = (ParameterizedType) iface;
            if (type.getRawType() == SerializableComparable.class) {
                return (Class<?>) type.getActualTypeArguments()[0];
            }
        }
        throw new RuntimeException("SerializableComparable interface not found");
    }
}
