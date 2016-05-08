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
import java.util.ArrayList;
import java.util.List;

public class ListTypeHandler implements TypeHandler<List> {
    private final TypeHandler handler;

    public ListTypeHandler(AnnotatedType annotatedType) throws TypeHandlerException {
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
        return Bytes.concat("List[".getBytes(), handler.getFingerprint(), "]".getBytes());
    }

    @Override
    public List deserialize(ByteBuffer buffer) {
        int sz = buffer.getInt();
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < sz; i++) {
            list.add(handler.deserialize(buffer));
        }
        return list;
    }

    @Override @SuppressWarnings("unchecked")
    public int size(List value) {
        int sz = SIZE_TAG_LENGTH;
        if (value != null) {
            for (Object o : value) {
                sz += handler.size(o);
            }
        }
        return sz;
    }

    @Override @SuppressWarnings("unchecked")
    public void serialize(List value, ByteBuffer buffer) {
        if (value == null) {
            buffer.putInt(0);
        } else {
            buffer.putInt(value.size());
            for (Object o : value) {
                handler.serialize(o, buffer);
            }
        }
    }

    @Override @SuppressWarnings("unchecked")
    public int comparableSize(List value) {
        int sz = 0;
        if (value != null) {
            for (Object o : value) {
                sz += handler.comparableSize(o);
            }
        }
        return sz;
    }

    @Override @SuppressWarnings("unchecked")
    public void serializeComparable(List value, ByteBuffer buffer) {
        if (value == null) {
        } else {
            for (Object o : value) {
                handler.serializeComparable(o, buffer);
            }
        }
    }
}
