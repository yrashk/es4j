/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.ObjectDeserializer;
import com.eventsourcing.layout.ObjectSerializer;
import com.eventsourcing.layout.Serialization;
import com.eventsourcing.layout.types.ObjectTypeHandler;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.eventsourcing.layout.TypeHandler.*;

public class BinarySerialization extends Serialization {

    private static final BinarySerialization BINARY_SERIALIZATION = new BinarySerialization();
    public static final int SIZE_TAG_LENGTH = 4;

    private BinarySerialization() {
        addDeserializer(BIG_DECIMAL_TYPE_HANDLER, new BigDecimalBinaryDeserializer());
        addSerializer(BIG_DECIMAL_TYPE_HANDLER, new BigDecimalBinarySerializer());
        addDeserializer(BOOLEAN_TYPE_HANDLER, new BooleanBinaryDeserializer());
        addSerializer(BOOLEAN_TYPE_HANDLER, new BooleanBinarySerializer());
        addDeserializer(BYTE_ARRAY_TYPE_HANDLER, new ByteArrayBinaryDeserializer());
        addSerializer(BYTE_ARRAY_TYPE_HANDLER, new ByteArrayBinarySerializer());
        addDeserializer(BYTE_TYPE_HANDLER, new ByteBinaryDeserializer());
        addSerializer(BYTE_TYPE_HANDLER, new ByteBinarySerializer());
        addDeserializer(DATE_TYPE_HANDLER, new DateBinaryDeserializer());
        addSerializer(DATE_TYPE_HANDLER, new DateBinarySerializer());
        addDeserializer(DOUBLE_TYPE_HANDLER, new DoubleBinaryDeserializer());
        addSerializer(DOUBLE_TYPE_HANDLER, new DoubleBinarySerializer());
        addDeserializer(ENUM_TYPE_HANDLER, new EnumBinaryDeserializer());
        addSerializer(ENUM_TYPE_HANDLER, new EnumBinarySerializer());
        addDeserializer(FLOAT_TYPE_HANDLER, new FloatBinaryDeserializer());
        addSerializer(FLOAT_TYPE_HANDLER, new FloatBinarySerializer());
        addDeserializer(INTEGER_TYPE_HANDLER, new IntegerBinaryDeserializer());
        addSerializer(INTEGER_TYPE_HANDLER, new IntegerBinarySerializer());
        addDeserializer(LIST_TYPE_HANDLER, new ListBinaryDeserializer());
        addSerializer(LIST_TYPE_HANDLER, new ListBinarySerializer());
        addDeserializer(LONG_TYPE_HANDLER, new LongBinaryDeserializer());
        addSerializer(LONG_TYPE_HANDLER, new LongBinarySerializer());
        addDeserializer(OBJECT_TYPE_HANDLER, new ObjectBinaryDeserializer<>());
        addSerializer(OBJECT_TYPE_HANDLER, new ObjectBinarySerializer<>());
        addDeserializer(OPTIONAL_TYPE_HANDLER, new OptionalBinaryDeserializer());
        addSerializer(OPTIONAL_TYPE_HANDLER, new OptionalBinarySerializer());
        addDeserializer(SHORT_TYPE_HANDLER, new ShortBinaryDeserializer());
        addSerializer(SHORT_TYPE_HANDLER, new ShortBinarySerializer());
        addDeserializer(STRING_TYPE_HANDLER, new StringBinaryDeserializer());
        addSerializer(STRING_TYPE_HANDLER, new StringBinarySerializer());
        addDeserializer(UUID_TYPE_HANDLER, new UUIDBinaryDeserializer());
        addSerializer(UUID_TYPE_HANDLER, new UUIDBinarySerializer());
    }

    public static BinarySerialization getInstance() {
        return BINARY_SERIALIZATION;
    }

    private Map<String, ObjectSerializer> objectSerializers = new HashMap<>();
    private Map<String, ObjectDeserializer> objectDeserializers = new HashMap<>();

    @SneakyThrows
    @Override public <T> ObjectSerializer<T> getSerializer(Class<?> klass) {
        @SuppressWarnings("unchecked")
        ObjectSerializer<T> objectSerializer = objectSerializers
                .computeIfAbsent(klass.getName(), (k) -> new RootObjectBinarySerializer<T>(new ObjectTypeHandler
                                                                                                   (klass)));
        return objectSerializer;
    }

    @Override public <T> ObjectDeserializer<T> getDeserializer(Class<?> klass, boolean allowReadonly) {
        @SuppressWarnings("unchecked")
        ObjectDeserializer<T> objectDeserializer = objectDeserializers
                .computeIfAbsent(klass.getName() + (allowReadonly ? "(r/o)": ""),
                                 (k) -> new RootObjectBinaryDeserializer<>(new ObjectTypeHandler(klass, allowReadonly)));
        return objectDeserializer;
    }

    private static class RootObjectBinarySerializer<T> extends ObjectBinarySerializer<T> {

        private final ObjectTypeHandler typeHandler;

        public RootObjectBinarySerializer(ObjectTypeHandler typeHandler) {this.typeHandler = typeHandler;}

        @Override public int size(T value) {
            return super.size(typeHandler, value);
        }

        @Override public void serialize(T value, ByteBuffer buffer) {
            super.serialize(typeHandler, value, buffer);
        }

        @Override public ByteBuffer serialize(T value) {
            return super.serialize(typeHandler, value);
        }
    }

    private static class RootObjectBinaryDeserializer<T> extends ObjectBinaryDeserializer<T> {
        private final ObjectTypeHandler typeHandler;

        public RootObjectBinaryDeserializer(ObjectTypeHandler typeHandler) {this.typeHandler = typeHandler;}

        @Override public T deserialize(ByteBuffer buffer) {
            return super.deserialize(typeHandler, buffer);
        }

        @Override public void deserialize(T object, ByteBuffer buffer) {
            super.deserialize(typeHandler, object, buffer);
        }
    }
}
