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

public class OptionalTypeHandler implements TypeHandler {
    @Getter
    private final TypeHandler wrappedHandler;

    public OptionalTypeHandler() {
        wrappedHandler = null;
    }

    public OptionalTypeHandler(AnnotatedType annotatedType) throws TypeHandlerException {
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("List type parameter should be specified");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
        Class<?> klass;
        if (arg.getType() instanceof ParameterizedType) {
            klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
        } else {
            klass = (Class<?>) arg.getType();
        }
        ResolvedType resolvedType = new TypeResolver().resolve(klass);
        wrappedHandler = TypeHandler.lookup(resolvedType, arg);
    }

    @Override
    public byte[] getFingerprint() {
        return Bytes.concat("Optional[".getBytes(), wrappedHandler.getFingerprint(), "]".getBytes());
    }

    @Override public int hashCode() {
        return "Optional".hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof OptionalTypeHandler && obj.hashCode() == hashCode();
    }

}
