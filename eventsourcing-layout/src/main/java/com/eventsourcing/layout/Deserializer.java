/**
 * Copyright 2016 Eventsourcing team
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
package com.eventsourcing.layout;

import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Layout deserializer
 * @param <T>
 */
public class Deserializer<T> implements com.eventsourcing.layout.core.Deserializer<T> {

    private final Layout<T> layout;

    public Deserializer(Layout<T> layout) throws NoEmptyConstructorException {
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
        this.layout = layout;
    }

    /**
     * Deserialize value of type <code>T</code> from a {@link ByteBuffer}
     * @param object object with {@link #layout} layout
     * @param buffer {@link ByteBuffer}
     */
    public void deserialize(T object, ByteBuffer buffer) {
        for (Property<T> property : layout.getProperties()) {
            property.set(object, property.getTypeHandler().deserialize(buffer));
        }
    }

    @Override
    @SneakyThrows
    public T deserialize(ByteBuffer buffer) {
        if (layout.getConstructor() != null) {
            return (T) layout.getConstructor().newInstance(layout.getProperties().stream().map(new PropertyFunction<>(buffer)).toArray());
        } else {
            T value = layout.getLayoutClass().newInstance();
            deserialize(value, buffer);
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
        public T apply(Property<T> prop) {
            return prop.getTypeHandler().deserialize(buffer);
        }
    }
}
