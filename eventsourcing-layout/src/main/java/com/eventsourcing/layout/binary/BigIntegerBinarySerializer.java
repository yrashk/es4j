/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.BigIntegerTypeHandler;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static com.eventsourcing.layout.binary.BinarySerialization.SIZE_TAG_LENGTH;

public class BigIntegerBinarySerializer implements Serializer<BigInteger, BigIntegerTypeHandler> {

    @Override
    public int size(BigInteger value) {
        if (value == null) {
            return size(BigInteger.ZERO);
        }
        return SIZE_TAG_LENGTH + value.toByteArray().length;
    }

    @Override
    public void serialize(BigInteger value, ByteBuffer buffer) {
        if (value == null) {
            serialize(BigInteger.ZERO, buffer);
        } else {
            byte[] bytes = value.toByteArray();
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
    }

}
