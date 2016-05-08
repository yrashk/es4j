/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class FloatTypeHandler implements TypeHandler<Float> {

    private static Optional<Integer> SIZE = Optional.of(4);

    @Override
    public byte[] getFingerprint() {
        return "Float".getBytes();
    }

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

    @Override
    public Float deserialize(ByteBuffer buffer) {
        return buffer.getFloat();
    }
}
