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

import java.nio.ByteBuffer;

/**
 * Layout serializer
 *
 * @param <T>
 */
public class ObjectBinarySerializer<T> implements Serializer.RequiresTypeHandler<T, ObjectTypeHandler>,
                                                  ObjectSerializer<T> {

    @Override
    public void serialize(ObjectTypeHandler typeHandler, T value, ByteBuffer buffer) {
        BinarySerialization serialization = BinarySerialization.getInstance();
        @SuppressWarnings("unchecked")
        Layout<T> layout = (Layout<T>)typeHandler.getLayout();
        for (Property<T> property : layout.getProperties()) {
            TypeHandler propertyTypeHandler = property.getTypeHandler();
            serialization.<T, TypeHandler>getSerializer(propertyTypeHandler)
                    .serialize(propertyTypeHandler, property.get(value), buffer);
        }
    }

    @Override
    public int size(ObjectTypeHandler typeHandler, T value) {
        int sz = 0;
        BinarySerialization serialization = BinarySerialization.getInstance();
        @SuppressWarnings("unchecked")
        Layout<T> layout = (Layout<T>)typeHandler.getLayout();
        for (Property<T> property : layout.getProperties()) {
            TypeHandler propertyTypeHandler = property.getTypeHandler();
            sz += serialization.<T, TypeHandler>getSerializer(propertyTypeHandler)
                    .size(propertyTypeHandler, property.get(value));
        }
        return sz;
    }


}
