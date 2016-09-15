/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.primitives.Bytes;
import lombok.Getter;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;

public class MapTypeHandler implements TypeHandler {

    @Getter
    private final TypeHandler wrappedKeyHandler;
    @Getter
    private final TypeHandler wrappedValueHandler;

    public MapTypeHandler() {
        wrappedKeyHandler = null;
        wrappedValueHandler = null;
    }

    public MapTypeHandler(AnnotatedType annotatedType) throws TypeHandlerException {
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("Map type parameters should be specified");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType key = parameterizedType.getAnnotatedActualTypeArguments()[0];
        AnnotatedType value = parameterizedType.getAnnotatedActualTypeArguments()[1];
        Class<?> keyClass;
        if (key.getType() instanceof ParameterizedType) {
            keyClass = (Class<?>) ((ParameterizedType) (key.getType())).getRawType();
        } else {
            keyClass = (Class<?>) key.getType();
        }
        Class<?> valueClass;
        if (value.getType() instanceof ParameterizedType) {
            valueClass = (Class<?>) ((ParameterizedType) (value.getType())).getRawType();
        } else {
            valueClass = (Class<?>) value.getType();
        }
        ResolvedType resolvedKeyType = new TypeResolver().resolve(keyClass);
        wrappedKeyHandler = TypeHandler.lookup(resolvedKeyType, key);
        ResolvedType resolvedValueType = new TypeResolver().resolve(valueClass);
        wrappedValueHandler = TypeHandler.lookup(resolvedValueType, value);

    }

    @Override
    public byte[] getFingerprint() {
        return Bytes.concat("Map[".getBytes(), wrappedKeyHandler.getFingerprint(), "]".getBytes(),"[".getBytes(),
                            wrappedValueHandler.getFingerprint(), "]".getBytes());
    }

    @Override public int hashCode() {
        return "Map".hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof MapTypeHandler && obj.hashCode() == hashCode();
    }
}
