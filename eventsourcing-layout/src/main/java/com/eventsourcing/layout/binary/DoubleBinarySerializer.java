/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.DoubleTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class DoubleBinarySerializer implements Serializer<Double, DoubleTypeHandler> {

    private static Optional<Integer> SIZE = Optional.of(8);

    @Override
    public int size(Double value) {
        return 8;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Double value, ByteBuffer buffer) {
        buffer.putDouble(value == null ? 0 : value);
    }

}
