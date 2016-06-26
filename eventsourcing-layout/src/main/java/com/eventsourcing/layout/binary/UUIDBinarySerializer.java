/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.UUIDTypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class UUIDBinarySerializer implements Serializer<UUID, UUIDTypeHandler> {

    private static final Optional<Integer> SIZE = Optional.of(16);

    @Override
    public int size(UUID value) {
        return 16;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(UUID value, ByteBuffer buffer) {
        if (value == null) {
            value = new UUID(0, 0);
        }
        buffer.putLong(value.getMostSignificantBits());
        buffer.putLong(value.getLeastSignificantBits());
    }

}
