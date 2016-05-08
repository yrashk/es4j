/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class BooleanTypeHandler implements TypeHandler<Boolean> {

    private static final Optional<Integer> SIZE = Optional.of(1);

    @Override
    public byte[] getFingerprint() {
        return "Boolean".getBytes();
    }

    @Override
    public int size(Boolean value) {
        return 1;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Boolean value, ByteBuffer buffer) {
        buffer.put((byte) (value == null ? 0 : (value ? 1 : 0)));
    }

    @Override
    public Boolean deserialize(ByteBuffer buffer) {
        return buffer.get() == 1;
    }
}
