/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.types.DateTypeHandler;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Optional;

public class DateBinarySerializer implements Serializer<Date, DateTypeHandler> {

    private static final Optional<Integer> SIZE = Optional.of(8);

    @Override
    public int size(Date value) {
        return 8;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }


    @Override
    public void serialize(Date value, ByteBuffer buffer) {
        buffer.putLong(value == null ? 0 : value.getTime());
    }

}
