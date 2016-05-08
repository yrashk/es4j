/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.primitives.Bytes;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.util.Optional;

public class OptionalTypeHandler implements TypeHandler<Optional> {
    private final TypeHandler handler;

    public OptionalTypeHandler(AnnotatedType annotatedType) throws TypeHandlerException {
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("List type parameter should be specified");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
        Class<?> klass;
        if (arg.getType() instanceof ParameterizedType) {
            klass = (Class<?>)((ParameterizedType)(arg.getType())).getRawType();
        } else {
            klass = (Class<?>) arg.getType();
        }
        ResolvedType resolvedType = new TypeResolver().resolve(klass);
        handler = TypeHandler.lookup(resolvedType, arg);
    }

    @Override
    public byte[] getFingerprint() {
        return Bytes.concat("Optional[".getBytes(), handler.getFingerprint(), "]".getBytes());
    }

    @Override
    public Optional deserialize(ByteBuffer buffer) {
        if (buffer.get() == 0) {
            return Optional.empty();
        }
        return Optional.of(handler.deserialize(buffer));
    }

    @Override @SuppressWarnings("unchecked")
    public int size(Optional value) {
        if (value == null) {
            return 1;
        }
        return value.isPresent() ? handler.size(value.get()) + 1 : 1;
    }

    @Override @SuppressWarnings("unchecked")
    public void serialize(Optional value, ByteBuffer buffer) {
        if (value == null) {
            buffer.put((byte) 0);
        } else {
            buffer.put((byte) (value.isPresent() ? 1 : 0));
            if (value.isPresent()) {
                handler.serialize(value.get(), buffer);
            }
        }
    }

    @Override @SuppressWarnings("unchecked")
    public int comparableSize(Optional value) {
        if (value == null) {
            return 0;
        }
        return value.isPresent() ? handler.comparableSize(value.get()) : 0;
    }

    @Override @SuppressWarnings("unchecked")
    public void serializeComparable(Optional value, ByteBuffer buffer) {
        if (value == null) {
        } else {
            if (value.isPresent()) {
                handler.serializeComparable(value.get(), buffer);
            }
        }
    }
}
