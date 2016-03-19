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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

/**
 * Interface for handling supported type T
 * @param <T>
 */
public interface TypeHandler<T> extends org.eventchain.layout.core.Serializer<T>,
                                        org.eventchain.layout.core.Deserializer<T> {

    int SIZE_TAG_LENGTH = 4;

    /**
     * Returns unique byte-array "fingerprint" representing type <code>T</code>
     * (used for hashing)
     * @return fingerprint
     */
    byte[] getFingerprint();

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
     *     <li><code>Enum</code></li>
     *     <li><code>List&lt;?&gt;</code></li>
     * </ul>
     * @param type
     * @return
     */
    static TypeHandler lookup(ResolvedType type, AnnotatedType annotatedType) {
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

        if (type.isInstanceOf(List.class)) {
            return new ListTypeHandler(annotatedType);
        }

        if (type.isArray() &&
           (type.getArrayElementType().isInstanceOf(Byte.TYPE) || type.getArrayElementType().isInstanceOf(Byte.class))) {
            return new ByteArrayTypeHandler(type.getArrayElementType().isPrimitive());
        }

        if (type.getErasedType().isEnum()) {
            return new EnumTypeHandler((Class<? extends Enum>) type.getErasedType());
        }

        return new UnknownTypeHandler(type.getErasedType());
    }
}
