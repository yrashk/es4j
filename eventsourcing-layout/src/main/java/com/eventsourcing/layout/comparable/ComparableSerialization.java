/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.comparable;

import com.eventsourcing.layout.*;
import com.eventsourcing.layout.binary.BinarySerialization;
import lombok.SneakyThrows;

import static com.eventsourcing.layout.TypeHandler.*;

public class ComparableSerialization extends Serialization {
    private static final ComparableSerialization COMPARABLE_SERIALIZATION = new ComparableSerialization();

    private ComparableSerialization() {
        addSerializer(BIG_DECIMAL_TYPE_HANDLER, new BigDecimalComparableSerializer());
        addSerializer(BYTE_ARRAY_TYPE_HANDLER, new ByteArrayComparableSerializer());
        addSerializer(LIST_TYPE_HANDLER, new ListComparableSerializer());
        addSerializer(OPTIONAL_TYPE_HANDLER, new OptionalComparableSerializer());
        addSerializer(STRING_TYPE_HANDLER, new StringComparableSerializer());
    }

    public static ComparableSerialization getInstance() {
        return COMPARABLE_SERIALIZATION;
    }

    @Override public <T, H extends TypeHandler> Deserializer<T, H> getDeserializer(H typeHandler) {
        throw new UnsupportedOperationException();
    }

    @Override public <T, H extends TypeHandler> Serializer<T, H> getSerializer(H typeHandler) {
        Serializer<T, H> serializer = super.getSerializer(typeHandler);
        if (serializer == null) {
            return BinarySerialization.getInstance().getSerializer(typeHandler);
        } else {
            return serializer;
        }
    }

    @SneakyThrows
    @Override public <T> ObjectSerializer<T> getSerializer(Class<?> klass) {
        throw new UnsupportedOperationException();
    }

    @Override public <T> ObjectDeserializer<T> getDeserializer(Class<?> klass, boolean allowReadonly) {
        throw new UnsupportedOperationException();
    }
    
}
