/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.eventsourcing.layout.types.*;
import com.fasterxml.classmate.ResolvedType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.AnnotatedType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for handling a supported type
 *
 */
public interface TypeHandler {

    BigDecimalTypeHandler BIG_DECIMAL_TYPE_HANDLER = new BigDecimalTypeHandler();
    BooleanTypeHandler BOOLEAN_TYPE_HANDLER = new BooleanTypeHandler();
    ByteArrayTypeHandler BYTE_ARRAY_TYPE_HANDLER = new ByteArrayTypeHandler(true);
    ByteTypeHandler BYTE_TYPE_HANDLER = new ByteTypeHandler();
    DateTypeHandler DATE_TYPE_HANDLER = new DateTypeHandler();
    DoubleTypeHandler DOUBLE_TYPE_HANDLER = new DoubleTypeHandler();
    EnumTypeHandler ENUM_TYPE_HANDLER = new EnumTypeHandler();
    ObjectTypeHandler OBJECT_TYPE_HANDLER = new ObjectTypeHandler();
    FloatTypeHandler FLOAT_TYPE_HANDLER = new FloatTypeHandler();
    IntegerTypeHandler INTEGER_TYPE_HANDLER = new IntegerTypeHandler();
    ListTypeHandler LIST_TYPE_HANDLER = new ListTypeHandler();
    OptionalTypeHandler OPTIONAL_TYPE_HANDLER = new OptionalTypeHandler();
    ShortTypeHandler SHORT_TYPE_HANDLER = new ShortTypeHandler();
    LongTypeHandler LONG_TYPE_HANDLER = new LongTypeHandler();
    StringTypeHandler STRING_TYPE_HANDLER = new StringTypeHandler();
    UUIDTypeHandler UUID_TYPE_HANDLER = new UUIDTypeHandler();


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
                return BYTE_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Short.TYPE) || type.isInstanceOf(Short.class)) {
                return SHORT_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Integer.TYPE) || type.isInstanceOf(Integer.class)) {
                return INTEGER_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Long.TYPE) || type.isInstanceOf(Long.class)) {
                return LONG_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Float.TYPE) || type.isInstanceOf(Float.class)) {
                return FLOAT_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Double.TYPE) || type.isInstanceOf(Double.class)) {
                return DOUBLE_TYPE_HANDLER;
            }

            if (type.isInstanceOf(BigDecimal.class)) {
                return BIG_DECIMAL_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Boolean.TYPE) || type.isInstanceOf(Boolean.class)) {
                return BOOLEAN_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Character.TYPE) || type.isInstanceOf(Character.class)) {
                throw new RuntimeException("Character type is not supported in RFC1/ELF");
            }

            if (type.isInstanceOf(String.class)) {
                return STRING_TYPE_HANDLER;
            }

            if (type.isInstanceOf(UUID.class)) {
                return UUID_TYPE_HANDLER;
            }

            if (type.isInstanceOf(Date.class)) {
                return DATE_TYPE_HANDLER;
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

            return new ObjectTypeHandler(type.getErasedType());
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
