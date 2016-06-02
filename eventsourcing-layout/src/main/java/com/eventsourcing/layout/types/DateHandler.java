/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Optional;

public class DateHandler implements TypeHandler<Date> {
    private static final Optional<Integer> SIZE = Optional.of(8);

    @Override
    public byte[] getFingerprint() {
        return "Timestamp".getBytes();
    }

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

    @Override
    public Date deserialize(ByteBuffer buffer) {
        return new Date(buffer.getLong());
    }
}
