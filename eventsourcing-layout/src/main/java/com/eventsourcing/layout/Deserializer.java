/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Layout deserializer
 *
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
     *
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
            return (T) layout.getConstructor().newInstance(
                    layout.getProperties().stream().map(new PropertyFunction<>(buffer)).toArray());
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
