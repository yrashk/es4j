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
import com.eventsourcing.layout.binary.BinarySerialization;
import com.eventsourcing.layout.types.OptionalTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class OptionalComparableSerializer implements Serializer.RequiresTypeHandler<Optional, OptionalTypeHandler> {

    @Override @SuppressWarnings("unchecked")
    public int size(OptionalTypeHandler typeHandler, Optional value) {
        if (value == null) {
            return 0;
        }
        BinarySerialization serialization = BinarySerialization.getInstance();
        TypeHandler handler = typeHandler.getWrappedHandler();
        Serializer<Object, TypeHandler> serializer = serialization.getSerializer(handler);
        return value.isPresent() ? serializer.size(handler, value.get()) : 0;
    }

    @Override @SuppressWarnings("unchecked")
    public void serialize(OptionalTypeHandler typeHandler, Optional value, ByteBuffer buffer) {
        if (value == null) {
        } else {
            if (value.isPresent()) {
                BinarySerialization serialization = BinarySerialization.getInstance();
                TypeHandler handler = typeHandler.getWrappedHandler();
                Serializer<Object, TypeHandler> serializer = serialization.getSerializer(handler);
                serializer.serialize(handler, value.get(), buffer);
            }
        }
    }
}
