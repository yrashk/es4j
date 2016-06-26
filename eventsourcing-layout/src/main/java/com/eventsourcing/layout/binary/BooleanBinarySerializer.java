/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.BooleanTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class BooleanBinarySerializer implements Serializer<Boolean, BooleanTypeHandler> {

    private static final Optional<Integer> SIZE = Optional.of(1);

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
}
