/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.EnumTypeHandler;

import java.nio.ByteBuffer;

public class EnumBinarySerializer implements Serializer<Enum, EnumTypeHandler> {

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
