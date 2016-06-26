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
import com.eventsourcing.layout.types.OptionalTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class OptionalBinaryDeserializer implements Deserializer.RequiresTypeHandler<Optional, OptionalTypeHandler> {

    @Override
    public Optional deserialize(OptionalTypeHandler typeHandler, ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return Optional.empty();
        }
        BinarySerialization serialization = BinarySerialization.getInstance();
        TypeHandler handler = typeHandler.getWrappedHandler();
        Deserializer<Object, TypeHandler> deserializer = serialization.getDeserializer(handler);
        return Optional.of(deserializer.deserialize(handler, buffer));
    }

}
