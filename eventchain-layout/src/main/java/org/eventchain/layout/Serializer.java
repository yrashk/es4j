package org.eventchain.layout;

import java.nio.ByteBuffer;

/**
 * Layout serializer
 * @param <T>
 */
public class Serializer<T> {

    private final Layout<T> layout;

    public Serializer(Layout<T> layout) {
        this.layout = layout;
    }

    /**
     * Serializes value of a type <code>T</code> to a newly allocated
     * {@link ByteBuffer}.
     *
     * If you already have a ByteBuffer of a size of at least {@link #size(T)},
     * you can use {@link #serialize(T, ByteBuffer)}.
     * @param value
     * @return New {@link ByteBuffer} instance
     */
    public ByteBuffer serialize(T value) {
        ByteBuffer buffer = ByteBuffer.allocate(size(value));
        serialize(value, buffer);
        return buffer;
    }

    /**
     * Serializes value of a type <code>T</code> to an existing {@link ByteBuffer}.
     *
     * Note that {@link ByteBuffer} should have at least {@link #size(T)} bytes available.
     *
     * @param value value to serialize
     * @param buffer existing {@link ByteBuffer}
     */
    public void serialize(T value, ByteBuffer buffer) {
        for (Property<T> property : layout.getProperties()) {
            property.getTypeHandler().serialize(property.get(value), buffer);
        }
    }

    /**
     * Returns size of the data of type <code>T</code>
     * @param value
     * @return size in bytes
     */
    public int size(T value) {
        int sz = 0;
        for (Property<T> property : layout.getProperties()) {
            sz += property.getTypeHandler().size(property.get(value));
        }
        return sz;
    }


}
