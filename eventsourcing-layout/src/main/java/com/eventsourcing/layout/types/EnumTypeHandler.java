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
import java.util.Arrays;

public class EnumTypeHandler implements TypeHandler<Enum> {

    private final Class<? extends Enum> klass;
    private final String[] enumNames;

    public EnumTypeHandler(Class<? extends Enum> klass) {
        this.klass = klass;
        enumNames = Arrays.stream(klass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    @Override
    public byte[] getFingerprint() {
        return new byte[]{(byte) 254};
    }

    @Override
    public Enum deserialize(ByteBuffer buffer) {
        return Enum.valueOf(klass, enumNames[buffer.getInt()]);
    }

    @Override
    public int size(Enum value) {
        return 4;
    }

    @Override
    public void serialize(Enum value, ByteBuffer buffer) {
        if (value == null) {
            buffer.putInt(0);
        } else {
            buffer.putInt(value.ordinal());
        }
    }
}
