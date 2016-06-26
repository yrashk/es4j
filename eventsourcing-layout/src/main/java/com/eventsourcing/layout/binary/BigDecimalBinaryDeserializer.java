/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.types.BigDecimalTypeHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigDecimalBinaryDeserializer implements Deserializer<BigDecimal, BigDecimalTypeHandler> {

    @Override
    public BigDecimal deserialize(ByteBuffer buffer) {
        int scale = buffer.getInt();
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new BigDecimal(new BigInteger(bytes), scale);
    }
}
