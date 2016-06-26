/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.google.common.primitives.Bytes;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumTypeHandler implements TypeHandler {

    @Getter
    private final Class<? extends Enum> enumClass;

    public EnumTypeHandler() {
        this.enumClass = Enum.class;
    }

    public EnumTypeHandler(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public byte[] getFingerprint() {
        byte[] shape = Arrays.asList(enumClass.getEnumConstants()).stream().
                sorted((o1, o2) -> o1.name().compareTo(o2.name())).
                                     map(c -> c.name() + ":" + c.ordinal()).
                                     collect(Collectors.joining(",")).getBytes();


        return Bytes.concat("Enum[".getBytes(), shape, "]".getBytes());
    }

    @Override public int hashCode() {
        return "Enum".hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof EnumTypeHandler && obj.hashCode() == hashCode();
    }

}
