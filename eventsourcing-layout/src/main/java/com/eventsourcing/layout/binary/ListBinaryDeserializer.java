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
import com.eventsourcing.layout.types.ListTypeHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ListBinaryDeserializer implements Deserializer.RequiresTypeHandler<List, ListTypeHandler> {

    @Override
    public List deserialize(ListTypeHandler typeHandler, ByteBuffer buffer) {
        int sz = buffer.getInt();
        List<Object> list = new ArrayList<>();
        BinarySerialization serialization = BinarySerialization.getInstance();
        TypeHandler handler = typeHandler.getWrappedHandler();
        Deserializer<Object, TypeHandler> deserializer = serialization.getDeserializer(handler);
        for (int i = 0; i < sz; i++) {
            list.add(deserializer.deserialize(handler, buffer));
        }
        return list;
    }

}
