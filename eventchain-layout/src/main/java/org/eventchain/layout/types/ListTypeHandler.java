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
package org.eventchain.layout.types;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.eventchain.layout.TypeHandler;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ListTypeHandler implements TypeHandler<List> {
    private final TypeHandler handler;

    public ListTypeHandler(AnnotatedType annotatedType) {
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("List type parameter should be specified");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
        // This is a fairly ugly hack, but I couldn't find anything better yet
        String classname = arg.getType().getTypeName().split("<")[0];
        Class<?> klass = Object.class;
        try {
          klass = Class.forName(classname);
        } catch (ClassNotFoundException e) {}
        ResolvedType resolvedType = new TypeResolver().resolve(klass);
        handler = TypeHandler.lookup(resolvedType, arg);
    }

    @Override
    public byte[] getFingerprint() {
        return new byte[]{(byte) 252};
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
}
