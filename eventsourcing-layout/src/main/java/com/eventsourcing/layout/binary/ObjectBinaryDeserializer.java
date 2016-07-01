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
import lombok.Value;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        BinarySerialization serialization = BinarySerialization.getInstance();

        @SuppressWarnings("unchecked")
        Layout<T> layout = (Layout<T>)typeHandler.getLayout();

        Map<Property<T>, Object> properties = new HashMap<>();

        for (Property<T> property: layout.getProperties()) {
            T v = serialization.<T, TypeHandler>getDeserializer(property.getTypeHandler())
                    .deserialize(property.getTypeHandler(), buffer);
            properties.put(property, v);
        }

        return layout.instantiate(properties);
    }

}
