/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.BigDecimalTypeHandler;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import static com.eventsourcing.layout.binary.BinarySerialization.SIZE_TAG_LENGTH;

public class BigDecimalBinarySerializer implements Serializer<BigDecimal, BigDecimalTypeHandler> {

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
