/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.layout.types.ListTypeHandler;

import java.nio.ByteBuffer;
import java.util.List;

public class ListBinarySerializer implements Serializer.RequiresTypeHandler<List, ListTypeHandler> {

    @Override @SuppressWarnings("unchecked")
    public int size(ListTypeHandler typeHandler, List value) {
        int sz = BinarySerialization.SIZE_TAG_LENGTH;
        if (value != null) {
            BinarySerialization serialization = BinarySerialization.getInstance();
            TypeHandler handler = typeHandler.getWrappedHandler();
            Serializer<Object, TypeHandler> serializer = serialization.getSerializer(handler);
            for (Object o : value) {
                sz += serializer.size(handler, o);
            }
        }
        return sz;
    }

    @Override @SuppressWarnings("unchecked")
    public void serialize(ListTypeHandler typeHandler, List value, ByteBuffer buffer) {
        if (value == null) {
            buffer.putInt(0);
        } else {
            buffer.putInt(value.size());
            BinarySerialization serialization = BinarySerialization.getInstance();
            TypeHandler handler = typeHandler.getWrappedHandler();
            Serializer<Object, TypeHandler> serializer = serialization.getSerializer(handler);
            for (Object o : value) {
                serializer.serialize(handler, o, buffer);
            }
        }
    }
}
