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

import com.fasterxml.classmate.ResolvedType;
import org.eventchain.layout.types.*;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for handling supported type T
 * @param <T>
 */
public interface TypeHandler<T> {

    int SIZE_TAG_LENGTH = 4;

    /**
     * Returns unique byte-array "fingerprint" representing type <code>T</code>
     * (used for hashing)
     * @return fingerprint
     */
    byte[] getFingerprint();

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
     * Deserializes value of type <code>T</code> from a {@link ByteBuffer}'s
     * current position
     *
     * @param buffer ByteBuffer
     * @return value
     */
    T deserialize(ByteBuffer buffer);

    /**
     * Looks up a type handler for supported types
     *
     * <h1>Supported types</h1>
     *
     * <ul>
     *     <li><code>Byte/byte</code></li>
     *     <li><code>Byte[]/byte[]</code></li>
     *     <li><code>Short/short</code></li>
     *     <li><code>Integer/int</code></li>
     *     <li><code>Long/long</code></li>
     *     <li><code>Float/float</code></li>
     *     <li><code>Double/double</code></li>
     *     <li><code>Boolean/boolean</code></li>
     *     <li><code>Character/char</code></li>
     *     <li><code>String</code></li>
     *     <li><code>UUID</code></li>
     * </ul>
     * @param type
     * @return
     */
    static TypeHandler lookup(ResolvedType type) {
        if (type.isInstanceOf(Byte.TYPE) || type.isInstanceOf(Byte.class)) {
            return new ByteTypeHandler();
        }

        if (type.isInstanceOf(Short.TYPE) || type.isInstanceOf(Short.class)) {
            return new ShortTypeHandler();
        }

        if (type.isInstanceOf(Integer.TYPE) || type.isInstanceOf(Integer.class)) {
            return new IntegerTypeHandler();
        }

        if (type.isInstanceOf(Long.TYPE) || type.isInstanceOf(Long.class)) {
            return new LongTypeHandler();
        }

        if (type.isInstanceOf(Float.TYPE) || type.isInstanceOf(Float.class)) {
            return new FloatTypeHandler();
        }

        if (type.isInstanceOf(Double.TYPE) || type.isInstanceOf(Double.class)) {
            return new DoubleTypeHandler();
        }

        if (type.isInstanceOf(Boolean.TYPE)|| type.isInstanceOf(Boolean.class)) {
            return new BooleanTypeHandler();
        }

        if (type.isInstanceOf(Character.TYPE)|| type.isInstanceOf(Character.class)) {
            return new CharacterTypeHandler();
        }

        if (type.isInstanceOf(String.class)) {
            return new StringTypeHandler();
        }

        if (type.isInstanceOf(UUID.class)) {
            return new UUIDTypeHandler();
        }

        if (type.isArray() &&
           (type.getArrayElementType().isInstanceOf(Byte.TYPE) || type.getArrayElementType().isInstanceOf(Byte.class))) {
            return new ByteArrayTypeHandler(type.getArrayElementType().isPrimitive());
        }

        return new UnknownTypeHandler();
    }
}
