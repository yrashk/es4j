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
package org.eventchain.layout;

import java.nio.ByteBuffer;

/**
 * Layout serializer
 * @param <T>
 */
public class Serializer<T> implements org.eventchain.layout.core.Serializer<T> {

    private final Layout<T> layout;

    public Serializer(Layout<T> layout) {
        this.layout = layout;
    }

    /**
     * Serializes value of a type <code>T</code> to a newly allocated
     * {@link ByteBuffer}.
     *
     * If you already have a ByteBuffer of a size of at least {@link #size(Object)},
     * you can use {@link #serialize(Object, ByteBuffer)}.
     * @param value
     * @return New {@link ByteBuffer} instance
     */
    public ByteBuffer serialize(T value) {
        ByteBuffer buffer = ByteBuffer.allocate(size(value));
        serialize(value, buffer);
        return buffer;
    }

    /**
     * Serializes value of a type <code>T</code> to an existing {@link ByteBuffer}.
     *
     * Note that {@link ByteBuffer} should have at least {@link #size(Object)} bytes available.
     *
     * @param value value to serialize
     * @param buffer existing {@link ByteBuffer}
     */
    public void serialize(T value, ByteBuffer buffer) {
        for (Property<T> property : layout.getProperties()) {
            property.getTypeHandler().serialize(property.get(value), buffer);
        }
    }

    /**
     * Returns size of the data of type <code>T</code>
     * @param value
     * @return size in bytes
     */
    public int size(T value) {
        int sz = 0;
        for (Property<T> property : layout.getProperties()) {
            sz += property.getTypeHandler().size(property.get(value));
        }
        return sz;
    }


}
