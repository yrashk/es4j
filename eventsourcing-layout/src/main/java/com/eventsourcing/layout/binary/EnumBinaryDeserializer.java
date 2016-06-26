/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.types.EnumTypeHandler;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class EnumBinaryDeserializer implements Deserializer.RequiresTypeHandler<Enum, EnumTypeHandler> {

    @Override
    public Enum deserialize(EnumTypeHandler typeHandler, ByteBuffer buffer) {
        Class<? extends Enum> enumClass = typeHandler.getEnumClass();
        String[] enumNames = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
        return Enum.valueOf(enumClass, enumNames[buffer.getInt()]);
    }

}
