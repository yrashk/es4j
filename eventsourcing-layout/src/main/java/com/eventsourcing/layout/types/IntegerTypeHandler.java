/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class IntegerTypeHandler implements TypeHandler<Integer> {

    private static final Optional<Integer> SIZE = Optional.of(4);

    @Override
    public byte[] getFingerprint() {
        return "Integer".getBytes();
    }

    @Override
    public int size(Integer value) {
        return 4;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Integer value, ByteBuffer buffer) {
        buffer.putInt(value == null ? 0 : value);
    }

    @Override
    public Integer deserialize(ByteBuffer buffer) {
        return buffer.getInt();
    }
}
