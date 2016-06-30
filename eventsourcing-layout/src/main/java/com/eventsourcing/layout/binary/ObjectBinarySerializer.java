/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.*;
import com.eventsourcing.layout.types.ObjectTypeHandler;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

/**
 * Layout serializer
 *
 * @param <T>
 */
public class ObjectBinarySerializer<T> implements Serializer.RequiresTypeHandler<T, ObjectTypeHandler<T>>,
                                                  ObjectSerializer<T> {

    @Override
    @SneakyThrows
    public void serialize(ObjectTypeHandler<T> typeHandler, T value, ByteBuffer buffer) {
        Layout<T> layout = typeHandler.getLayout();
        if (value == null) {
            serialize(typeHandler, layout.instantiate(), buffer);
        } else {
            BinarySerialization serialization = BinarySerialization.getInstance();
            for (Property<T> property : layout.getProperties()) {
                TypeHandler propertyTypeHandler = property.getTypeHandler();
                serialization.<T, TypeHandler>getSerializer(propertyTypeHandler)
                        .serialize(propertyTypeHandler, property.get(value), buffer);
            }
        }
    }

    @Override
    @SneakyThrows
    public int size(ObjectTypeHandler<T> typeHandler, T value) {
        Layout<T> layout = typeHandler.getLayout();
        if (value == null) {
            return size(typeHandler, layout.instantiate());
        }
        int sz = 0;
        BinarySerialization serialization = BinarySerialization.getInstance();
        for (Property<T> property : layout.getProperties()) {
            TypeHandler propertyTypeHandler = property.getTypeHandler();
            sz += serialization.<T, TypeHandler>getSerializer(propertyTypeHandler)
                    .size(propertyTypeHandler, property.get(value));
        }
        return sz;
    }


}
