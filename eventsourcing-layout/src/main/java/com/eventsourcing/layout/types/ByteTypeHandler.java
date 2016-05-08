/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class ByteTypeHandler implements TypeHandler<Byte> {

    private static final Optional<Integer> SIZE = Optional.of(1);

    @Override
    public byte[] getFingerprint() {
        return "Byte".getBytes();
    }

    @Override
    public int size(Byte value) {
        return 1;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Byte value, ByteBuffer buffer) {
        buffer.put(value == null ? 0 : value);
    }

    @Override
    public Byte deserialize(ByteBuffer buffer) {
        return buffer.get();
    }
}
