/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class LongTypeHandler implements TypeHandler<Long> {

    private static final Optional<Integer> SIZE = Optional.of(8);

    @Override
    public byte[] getFingerprint() {
        return "Long".getBytes();
    }

    @Override
    public int size(Long value) {
        return 8;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }


    @Override
    public void serialize(Long value, ByteBuffer buffer) {
        buffer.putLong(value == null ? 0 : value);
    }

    @Override
    public Long deserialize(ByteBuffer buffer) {
        return buffer.getLong();
    }
}
