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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigDecimalTypeHandler implements TypeHandler<BigDecimal> {
    @Override
    public byte[] getFingerprint() {
        return new byte[]{5, 1};
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
}
