/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.comparable;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.layout.types.MapTypeHandler;

import java.nio.ByteBuffer;
import java.util.Map;

public class MapComparableSerializer  implements Serializer.RequiresTypeHandler<Map, MapTypeHandler> {

    @Override @SuppressWarnings("unchecked")
    public int size(MapTypeHandler typeHandler, Map value) {
        int sz = 0;
        if (value != null) {
            ComparableSerialization serialization = ComparableSerialization.getInstance();
            TypeHandler keyHandler = typeHandler.getWrappedKeyHandler();
            TypeHandler valueHandler = typeHandler.getWrappedValueHandler();
            Serializer<Object, TypeHandler> keySerializer = serialization.getSerializer(keyHandler);
            Serializer<Object, TypeHandler> valueSerializer = serialization.getSerializer(valueHandler);
            for (Object o : value.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                sz += keySerializer.size(keyHandler, entry.getKey());
                sz += valueSerializer.size(valueHandler, entry.getValue());
            }
        }
        return sz;
    }

    @Override @SuppressWarnings("unchecked")
    public void serialize(MapTypeHandler typeHandler, Map value, ByteBuffer buffer) {
        if (value == null) {
        } else {
            ComparableSerialization serialization = ComparableSerialization.getInstance();
            TypeHandler keyHandler = typeHandler.getWrappedKeyHandler();
            TypeHandler valueHandler = typeHandler.getWrappedValueHandler();
            Serializer<Object, TypeHandler> keySerializer = serialization.getSerializer(keyHandler);
            Serializer<Object, TypeHandler> valueSerializer = serialization.getSerializer(valueHandler);
            for (Object o : value.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                keySerializer.serialize(keyHandler, entry.getKey(), buffer);
                valueSerializer.serialize(valueHandler, entry.getValue(), buffer);
            }
        }
    }
}
