/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.core;

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
