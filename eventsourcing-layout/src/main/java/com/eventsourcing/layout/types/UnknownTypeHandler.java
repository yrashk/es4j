/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.Serializer;
import com.eventsourcing.layout.TypeHandler;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;

public class UnknownTypeHandler implements TypeHandler {

    private final Class klass;
    private final Layout layout;
    private final Serializer serializer;
    private final Deserializer deserializer;

    @SneakyThrows @SuppressWarnings("unchecked")
    public UnknownTypeHandler(Class klass) {
        this.klass = klass;
        layout = new Layout<>(klass);
        serializer = new Serializer<>(layout);
        deserializer = new Deserializer<>(layout);
    }

    @Override
    public byte[] getFingerprint() {
        return "Unknown".getBytes();
    }

    @Override @SneakyThrows
    @SuppressWarnings("unchecked")
    public int size(Object value) {
        if (value == null) {
            return size(klass.newInstance());
        }
        return serializer.size(value);
    }

    @Override @SneakyThrows @SuppressWarnings("unchecked")
    public void serialize(Object value, ByteBuffer buffer) {
        if (value != null) {
            serializer.serialize(value, buffer);
        } else {
            serialize(klass.newInstance(), buffer);
        }
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        return deserializer.deserialize(buffer);
    }
}
