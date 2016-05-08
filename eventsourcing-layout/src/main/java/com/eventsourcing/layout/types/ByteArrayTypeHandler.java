/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.*;

public class ByteArrayTypeHandler implements TypeHandler {

    private boolean primitive;

    public ByteArrayTypeHandler(boolean primitive) {
        this.primitive = primitive;
    }

    @Override
    public byte[] getFingerprint() {
        return "ByteArray".getBytes();
    }

    @Override
    public int size(Object value) {
        return SIZE_TAG_LENGTH + getPrimitiveArray(value).length;
    }

    @Override
    public void serialize(Object value, ByteBuffer buffer) {
        byte[] bytes = getPrimitiveArray(value);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        if (primitive) {
            return bytes;
        } else {
            return toObject(bytes);
        }
    }

    private byte[] getPrimitiveArray(Object value) {
        if (value instanceof byte[]) {
            return nullToEmpty((byte[])value);
        }
        if (value instanceof Byte[]) {
            return nullToEmpty(toPrimitive((Byte[]) value));
        }
        if (value == null) {
            return new byte[]{};
        }
        throw new IllegalArgumentException(value.toString());
    }

    @Override
    public int comparableSize(Object value) {
        return size(value) - SIZE_TAG_LENGTH;
    }

    @Override
    public void serializeComparable(Object value, ByteBuffer buffer) {
        byte[] bytes = getPrimitiveArray(value);
        buffer.put(bytes);
    }
}
