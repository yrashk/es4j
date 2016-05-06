/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
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
