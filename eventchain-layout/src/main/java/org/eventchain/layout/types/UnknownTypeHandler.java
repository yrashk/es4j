/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain.layout.types;

import lombok.SneakyThrows;
import org.eventchain.layout.Deserializer;
import org.eventchain.layout.Layout;
import org.eventchain.layout.Serializer;
import org.eventchain.layout.TypeHandler;

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
        return new byte[253];
    }

    @Override @SuppressWarnings("unchecked")
    public int size(Object value) {
        if (value == null) {
            return 1;
        }
        return 1 + serializer.size(value);
    }

    @Override @SuppressWarnings("unchecked")
    public void serialize(Object value, ByteBuffer buffer) {
        if (value != null) {
            buffer.put((byte) 1);
            serializer.serialize(value, buffer);
        } else {
            buffer.put((byte) 0);
        }
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        if (buffer.get() == (byte)1) {
            return deserializer.deserialize(buffer);
        } else {
            return null;
        }
    }
}
