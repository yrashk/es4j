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
import java.util.function.Function;

/**
 * Layout deserializer
 *
 * @param <T>
 */
public class ObjectBinaryDeserializer<T> implements Deserializer.RequiresTypeHandler<T, ObjectTypeHandler>,
                                                    ObjectDeserializer<T> {

    protected void checkLayout(Layout<T> layout) throws NoEmptyConstructorException {
        if (layout.isReadOnly()) {
            throw new IllegalArgumentException("Read-only layout");
        }
        if (layout.getConstructor() == null && layout.getLayoutClass().getConstructors().length > 0) {
            try {
                layout.getLayoutClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new NoEmptyConstructorException();
            }
        }
    }

    @SneakyThrows
    public void deserialize(ObjectTypeHandler typeHandler, T object, ByteBuffer buffer) {
        BinarySerialization serialization = BinarySerialization.getInstance();
        Layout<T> layout = (Layout<T>)typeHandler.getLayout();
        checkLayout(layout);
        for (Property<T> property : layout.getProperties()) {
            property.set(object, serialization.getDeserializer(property.getTypeHandler()).
                    deserialize(property.getTypeHandler(), buffer));
        }
    }

    @Override
    @SneakyThrows
    public T deserialize(ObjectTypeHandler typeHandler, ByteBuffer buffer) {
        Layout<T> layout = (Layout<T>)typeHandler.getLayout();
        checkLayout(layout);
        if (layout.getConstructor() != null) {
            return (T) layout.getConstructor().newInstance(
                    layout.getProperties().stream().map(new PropertyFunction<>(buffer)).toArray());
        } else {
            T value = layout.getLayoutClass().newInstance();
            deserialize(typeHandler, value, buffer);
            return value;
        }
    }

    public static class NoEmptyConstructorException extends Exception {}

    private class PropertyFunction<T> implements Function<Property<T>, T> {
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
