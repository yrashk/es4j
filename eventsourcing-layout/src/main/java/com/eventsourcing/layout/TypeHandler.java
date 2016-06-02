/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.eventsourcing.layout.core.Deserializer;
import com.eventsourcing.layout.types.*;
import com.fasterxml.classmate.ResolvedType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for handling supported type T
 *
 * @param <T>
 */
public interface TypeHandler<T> extends com.eventsourcing.layout.core.Serializer<T>,
        Deserializer<T> {

    int SIZE_TAG_LENGTH = 4;

    /**
     * Returns unique byte-array "fingerprint" representing type <code>T</code>
     * (used for hashing)
     *
     * @return fingerprint
     */
    byte[] getFingerprint();

    /**
     * Looks up a type handler for supported types
     * <p>
     * <h1>Supported types</h1>
     * <p>
     * <ul>
     * <li><code>Byte/byte</code></li>
     * <li><code>Byte[]/byte[]</code></li>
     * <li><code>Short/short</code></li>
     * <li><code>Integer/int</code></li>
     * <li><code>Long/long</code></li>
     * <li><code>Float/float</code></li>
     * <li><code>Double/double</code></li>
     * <li><code>BigDecimal</code></li>
     * <li><code>Boolean/boolean</code></li>
     * <li><code>String</code></li>
     * <li><code>UUID</code></li>
     * <li><code>java.util.Date</code></li>
     * <li><code>Enum</code></li>
     * <li><code>List&lt;?&gt;</code></li>
     * <li><code>Optional&lt;?&gt;</code></li>
     * </ul>
     *
     * @param type
     * @return
     */
    static TypeHandler lookup(ResolvedType type, AnnotatedType annotatedType) throws TypeHandlerException {
        try {
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

            if (type.isInstanceOf(BigDecimal.class)) {
                return new BigDecimalTypeHandler();
            }

            if (type.isInstanceOf(Boolean.TYPE) || type.isInstanceOf(Boolean.class)) {
                return new BooleanTypeHandler();
            }

            if (type.isInstanceOf(Character.TYPE) || type.isInstanceOf(Character.class)) {
                throw new RuntimeException("Character type is not supported in RFC1/ELF");
            }

            if (type.isInstanceOf(String.class)) {
                return new StringTypeHandler();
            }

            if (type.isInstanceOf(UUID.class)) {
                return new UUIDTypeHandler();
            }

            if (type.isInstanceOf(Date.class)) {
                return new DateHandler();
            }

            if (type.isInstanceOf(List.class)) {
                return new ListTypeHandler(annotatedType);
            }

            if (type.isInstanceOf(Optional.class)) {
                return new OptionalTypeHandler(annotatedType);
            }


            if (type.isArray() &&
                    (type.getArrayElementType().isInstanceOf(Byte.TYPE) || type.getArrayElementType()
                                                                               .isInstanceOf(Byte.class))) {
                return new ByteArrayTypeHandler(type.getArrayElementType().isPrimitive());
            }

            if (type.getErasedType().isEnum()) {
                return new EnumTypeHandler((Class<? extends Enum>) type.getErasedType());
            }

            return new UnknownTypeHandler(type.getErasedType());
        } catch (Exception e) {
            throw new TypeHandlerException(e, type);
        }
    }

    @AllArgsConstructor class TypeHandlerException extends Exception {
        @Getter
        private Exception exception;
        @Getter
        private ResolvedType resolvedType;

        @Override
        public String getMessage() {
            return "Attempting to resolve type " + resolvedType.toString() + " resulted in " + exception
                    .getClass() + ": " + exception.getMessage();
        }
    }
}
