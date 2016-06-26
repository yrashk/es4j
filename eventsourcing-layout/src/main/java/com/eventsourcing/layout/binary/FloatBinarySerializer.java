/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.FloatTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class FloatBinarySerializer implements Serializer<Float, FloatTypeHandler> {

    private static Optional<Integer> SIZE = Optional.of(4);

    @Override
    public int size(Float value) {
        return 4;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Float value, ByteBuffer buffer) {
        buffer.putFloat(value == null ? 0 : value);
    }

}
