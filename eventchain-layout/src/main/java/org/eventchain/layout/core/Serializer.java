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
package org.eventchain.layout.core;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface Serializer<T> {
    /**
     * @param value value to be serialized
     * @return serialized value size in bytes
     */
    int size(T value);

    /**
     * If type is of a constant size, should return a non-empty
     * {@link Optional} containing value size in bytes
     * @return Optional constant size
     */
    default Optional<Integer> constantSize() {
        return Optional.empty();
    }


    /**
     * Serializes value of type <code>T</code> to a {@link ByteBuffer}.
     *
     * {@link ByteBuffer} should be of a correct size. The size can be obtained
     * from {@link #size(Object)}
     * @param value value to serialize
     * @param buffer ByteBuffer
     */
    void serialize(T value, ByteBuffer buffer);

    /**
     * @param value value to be serialized
     * @return serialized comparable value size in bytes
     */
    default int comparableSize(T value) {
        return size(value);
    }

    /**
     * Serializes a comparable (sortable, etc.) value of type <code>T</code> to a {@link ByteBuffer}.
     *
     * {@link ByteBuffer} should be of a correct size. The size can be obtained
     * from {@link #comparableSize(Object)} (Object)}
     * @param value value to serialize
     * @param buffer ByteBuffer
     */
    default void serializeComparable(T value, ByteBuffer buffer) {
        serialize(value, buffer);
    }

}
