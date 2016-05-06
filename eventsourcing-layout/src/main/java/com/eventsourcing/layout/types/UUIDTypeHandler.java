/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class UUIDTypeHandler implements TypeHandler<UUID> {

    private static final Optional<Integer> SIZE = Optional.of(16);

    @Override
    public byte[] getFingerprint() {
        return "UUID".getBytes();
    }

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

    @Override
    public UUID deserialize(ByteBuffer buffer) {
        long most = buffer.getLong();
        long least = buffer.getLong();
        return new UUID(most, least);
    }
}
