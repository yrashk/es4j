/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.comparable;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.StringTypeHandler;

import java.nio.ByteBuffer;

public class StringComparableSerializer implements Serializer<String, StringTypeHandler> {

    @Override
    public int size(String value) {
        return value == null ? 0 : value.getBytes().length;
    }

    @Override
    public void serialize(String value, ByteBuffer buffer) {
        if (value != null) {
            buffer.put(value.getBytes());
        }
    }
}
