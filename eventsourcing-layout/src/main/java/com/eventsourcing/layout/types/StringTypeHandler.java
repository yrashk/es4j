/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;

public class StringTypeHandler implements TypeHandler<String> {
    @Override
    public byte[] getFingerprint() {
        return "String".getBytes();
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
