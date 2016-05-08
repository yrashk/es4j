/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigDecimalTypeHandler implements TypeHandler<BigDecimal> {
    @Override
    public byte[] getFingerprint() {
        return "BigDecimal".getBytes();
    }

    @Override
    public BigDecimal deserialize(ByteBuffer buffer) {
        int scale = buffer.getInt();
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new BigDecimal(new BigInteger(bytes), scale);
    }

    @Override
    public int size(BigDecimal value) {
        if (value == null) {
            return size(BigDecimal.ZERO);
        }
        return SIZE_TAG_LENGTH + 4 + value.unscaledValue().toByteArray().length;
    }

    @Override
    public void serialize(BigDecimal value, ByteBuffer buffer) {
        if (value == null) {
            serialize(BigDecimal.ZERO, buffer);
        } else {
            buffer.putInt(value.scale());
            byte[] bytes = value.unscaledValue().toByteArray();
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
    }

    @Override
    public int comparableSize(BigDecimal value) {
        if (value == null) {
            return comparableSize(BigDecimal.ZERO);
        }
        return value.unscaledValue().toByteArray().length;
    }

    @Override
    public void serializeComparable(BigDecimal value, ByteBuffer buffer) {
        if (value == null) {
            serializeComparable(BigDecimal.ZERO, buffer);
        } else {
            buffer.put(value.unscaledValue().toByteArray());
        }
    }
}
