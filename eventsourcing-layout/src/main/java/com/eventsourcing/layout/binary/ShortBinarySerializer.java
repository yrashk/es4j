/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.ShortTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class ShortBinarySerializer implements Serializer<Short, ShortTypeHandler> {

    private static final Optional<Integer> SIZE = Optional.of(2);

    @Override
    public int size(Short value) {
        return 2;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }


    @Override
    public void serialize(Short value, ByteBuffer buffer) {
        buffer.putShort(value == null ? 0 : value);
    }

}
