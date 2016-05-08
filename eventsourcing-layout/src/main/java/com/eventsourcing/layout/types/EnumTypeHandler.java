/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumTypeHandler implements TypeHandler<Enum> {

    private final Class<? extends Enum> klass;
    private final String[] enumNames;

    public EnumTypeHandler(Class<? extends Enum> klass) {
        this.klass = klass;
        enumNames = Arrays.stream(klass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    @Override
    public byte[] getFingerprint() {
        byte[] shape = Arrays.asList(klass.getEnumConstants()).stream().
                sorted((o1, o2) -> o1.name().compareTo(o2.name())).
                                     map(c -> c.name() + ":" + c.ordinal()).
                                     collect(Collectors.joining(",")).getBytes();


        return Bytes.concat("Enum[".getBytes(), shape, "]".getBytes());
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
