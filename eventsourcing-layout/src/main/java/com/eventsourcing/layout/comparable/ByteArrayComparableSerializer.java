/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.comparable;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.ByteArrayTypeHandler;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

public class ByteArrayComparableSerializer implements Serializer<Object, ByteArrayTypeHandler> {

    @Override
    public int size(Object value) {
        return getPrimitiveArray(value).length;
    }

    @Override
    public void serialize(Object value, ByteBuffer buffer) {
        byte[] bytes = getPrimitiveArray(value);
        buffer.put(bytes);
    }

    private byte[] getPrimitiveArray(Object value) {
        if (value instanceof byte[]) {
            return nullToEmpty((byte[]) value);
        }
        if (value instanceof Byte[]) {
            return nullToEmpty(toPrimitive((Byte[]) value));
        }
        if (value == null) {
            return new byte[]{};
        }
        throw new IllegalArgumentException(value.toString());
    }
}
