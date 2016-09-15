/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.layout.types.MapTypeHandler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MapBinaryDeserializer implements Deserializer.RequiresTypeHandler<Map, MapTypeHandler> {

    @Override
    public Map deserialize(MapTypeHandler typeHandler, ByteBuffer buffer) {
        int sz = buffer.getInt();
        Map<Object, Object> map = new HashMap<>();
        BinarySerialization serialization = BinarySerialization.getInstance();
        TypeHandler keyHandler = typeHandler.getWrappedKeyHandler();
        TypeHandler valueHandler = typeHandler.getWrappedValueHandler();
        Deserializer<Object, TypeHandler> keyDeserializer = serialization.getDeserializer(keyHandler);
        Deserializer<Object, TypeHandler> valueDeserializer = serialization.getDeserializer(valueHandler);
        for (int i = 0; i < sz; i++) {
            Object key = keyDeserializer.deserialize(keyHandler, buffer);
            Object value = valueDeserializer.deserialize(valueHandler, buffer);
            map.put(key, value);
        }
        return map;
    }
}
