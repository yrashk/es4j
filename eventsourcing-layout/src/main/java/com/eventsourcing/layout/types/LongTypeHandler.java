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

public class LongTypeHandler implements TypeHandler {

    @Override
    public byte[] getFingerprint() {
        return "Long".getBytes();
    }

    @Override public int hashCode() {
        return ByteBuffer.wrap(getFingerprint()).hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof LongTypeHandler && obj.hashCode() == hashCode();
    }

}
