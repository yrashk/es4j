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

public class DoubleTypeHandler implements TypeHandler<Double> {

    private static Optional<Integer> SIZE = Optional.of(8);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{5};
    }

    @Override
    public int size(Double value) {
        return 8;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Double value, ByteBuffer buffer) {
        buffer.putDouble(value == null ? 0 : value);
    }

    @Override
    public Double deserialize(ByteBuffer buffer) {
        return buffer.getDouble();
    }
}
