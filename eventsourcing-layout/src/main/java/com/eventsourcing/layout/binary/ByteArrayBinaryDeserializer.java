/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.types.ByteArrayTypeHandler;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.toObject;

public class ByteArrayBinaryDeserializer implements Deserializer.RequiresTypeHandler<Object, ByteArrayTypeHandler> {

    @Override
    public Object deserialize(ByteArrayTypeHandler typeHandler, ByteBuffer buffer) {
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        if (typeHandler.isPrimitive()) {
            return bytes;
        } else {
            return toObject(bytes);
        }
    }
}
