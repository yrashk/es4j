/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import lombok.Getter;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

public class ByteArrayTypeHandler implements TypeHandler, PrimitiveTypeHandler {

    @Getter
    private boolean primitive;

    public ByteArrayTypeHandler(boolean primitive) {
        this.primitive = primitive;
    }

    @Override
    public byte[] getFingerprint() {
        return "ByteArray".getBytes();
    }

    @Override public int hashCode() {
        return ByteBuffer.wrap(getFingerprint()).hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof ByteArrayTypeHandler && obj.hashCode() == hashCode();
    }

    @Override public Object toPrimitive(Object o) {
        if (o instanceof byte[]) {
            return nullToEmpty((byte[]) o);
        }
        if (o instanceof Byte[]) {
            return nullToEmpty(org.apache.commons.lang3.ArrayUtils.toPrimitive((Byte[]) o));
        }
        if (o == null) {
            return new byte[]{};
        }
        throw new IllegalArgumentException(o.toString());
    }

    @Override public Object toObject(Object o) {
        if (o instanceof byte[]) {
            return org.apache.commons.lang3.ArrayUtils.toObject(nullToEmpty((byte[]) o));
        }
        if (o instanceof Byte[]) {
            return nullToEmpty((Byte[])o);
        }
        if (o == null) {
            return new Byte[]{};
        }
        throw new IllegalArgumentException(o.toString());
    }
}
