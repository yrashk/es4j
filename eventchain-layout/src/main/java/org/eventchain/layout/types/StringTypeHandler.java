/**
 * Copyright 2016 Eventchain team
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
package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;

public class StringTypeHandler implements TypeHandler<String> {
    @Override
    public byte[] getFingerprint() {
        return new byte[]{8};
    }

    @Override
    public int size(String value) {
        return value == null ? SIZE_TAG_LENGTH : SIZE_TAG_LENGTH + value.getBytes().length;
    }

    @Override
    public void serialize(String value, ByteBuffer buffer) {
        buffer.putInt(value == null ? 0 : value.getBytes().length);
        if (value != null) {
            buffer.put(value.getBytes());
        }
    }

    @Override
    public String deserialize(ByteBuffer buffer) {
        int len = buffer.getInt();
        byte[] buf = new byte[len];
        buffer.get(buf);
        return new String(buf);
    }

    @Override
    public int comparableSize(String value) {
        return size(value) - SIZE_TAG_LENGTH;
    }

    @Override
    public void serializeComparable(String value, ByteBuffer buffer) {
        if (value != null) {
            buffer.put(value.getBytes());
        }
    }
}
