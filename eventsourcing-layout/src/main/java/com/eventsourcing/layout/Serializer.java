/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface Serializer<T, H extends TypeHandler> {

    interface RequiresTypeHandler<T, H extends TypeHandler> extends Serializer<T,H> {
        default int size(T value) {
            throw new UnsupportedOperationException();
        }
        default void serialize(T value, ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * @param value value to be serialized
     * @return serialized value size in bytes
     */
    int size(T value);

    /**
     * @param typeHandler instance of {@link TypeHandler}
     * @param value value to be serialized
     * @return serialized value size in bytes
     */

    default int size(H typeHandler, T value) {
        return size(value);
    }

    /**
     * If type is of a constant size, should return a non-empty
     * {@link Optional} containing value size in bytes
     *
     * @return Optional constant size
     */
    default Optional<Integer> constantSize() {
        return Optional.empty();
    }


    /**
     * Serialize to a byte buffer
     * @param typeHandler
     * @param value
     * @return
     */
    default ByteBuffer serialize(H typeHandler, T value) {
        ByteBuffer buffer = ByteBuffer.allocate(size(typeHandler, value));
        serialize(typeHandler, value, buffer);
        return buffer;
    }

    /**
     * Serialize to a byte buffer
     * @param value
     * @return
     */
    default ByteBuffer serialize(T value) {
        ByteBuffer buffer = ByteBuffer.allocate(size(value));
        serialize(value, buffer);
        return buffer;
    }
    /**
     * Serializes value of type <code>T</code> to a {@link ByteBuffer}.
     * <p>
     * {@link ByteBuffer} should be of a correct size. The size can be obtained
     * from {@link #size(Object)}
     *
     * @param value  value to serialize
     * @param buffer ByteBuffer
     */
    void serialize(T value, ByteBuffer buffer);

    /**
     * Serializes value of type <code>T</code> to a {@link ByteBuffer}.
     * <p>
     * {@link ByteBuffer} should be of a correct size. The size can be obtained
     * from {@link #size(Object)}
     *
     * @param typeHandler Instance of {@link TypeHandler}
     * @param value  value to serialize
     * @param buffer ByteBuffer
     */
    default void serialize(H typeHandler, T value, ByteBuffer buffer) {
        serialize(value, buffer);
    }

}
