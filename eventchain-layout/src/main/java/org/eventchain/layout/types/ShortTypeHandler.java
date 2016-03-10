package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class ShortTypeHandler implements TypeHandler<Short> {

    private static final Optional<Integer> SIZE = Optional.of(2);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{1};
    }

    @Override
    public int size(Short value) {
        return 2;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }


    @Override
    public void serialize(Short value, ByteBuffer buffer) {
        buffer.putShort(value == null ? 0 : value);
    }

    @Override
    public Short deserialize(ByteBuffer buffer) {
        return buffer.getShort();
    }
}
