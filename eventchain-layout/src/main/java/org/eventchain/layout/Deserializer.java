package org.eventchain.layout;

import java.nio.ByteBuffer;

/**
 * Layout deserializer
 * @param <T>
 */
public class Deserializer<T> {

    private final Layout<T> layout;

    public Deserializer(Layout<T> layout) {
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

}
