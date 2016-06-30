/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.binary;

import com.eventsourcing.layout.*;
import com.eventsourcing.layout.types.ObjectTypeHandler;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Layout deserializer
 *
 * @param <T>
 */
public class ObjectBinaryDeserializer<T> implements Deserializer.RequiresTypeHandler<T, ObjectTypeHandler>,
                                                    ObjectDeserializer<T> {


    @Override
    @SneakyThrows
    public T deserialize(ObjectTypeHandler typeHandler, ByteBuffer buffer) {
        @SuppressWarnings("unchecked")
        Layout<T> layout = (Layout<T>)typeHandler.getLayout();
        List<Property<T>> properties = layout.getProperties();
        int[] order = layout.getConstructorProperties().stream()
                           .mapToInt(properties::indexOf).toArray();

        List<T> values = layout.getProperties().stream().map(new PropertyFunction<>(buffer))
                                .collect(Collectors.toList());

       return layout.getConstructor().newInstance(IntStream.of(order).mapToObj(values::get).toArray());
    }

    private static class PropertyFunction<T> implements Function<Property<T>, T> {
        private final ByteBuffer buffer;

        public PropertyFunction(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public T apply(Property<T> property) {
            BinarySerialization serialization = BinarySerialization.getInstance();
            return serialization.<T, TypeHandler>getDeserializer(property.getTypeHandler())
                    .deserialize(property.getTypeHandler(), buffer);
        }
    }
}
