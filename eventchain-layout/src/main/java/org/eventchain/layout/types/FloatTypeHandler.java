package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class FloatTypeHandler implements TypeHandler<Float> {

    private static Optional<Integer> SIZE = Optional.of(4);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{4};
    }

    @Override
    public int size(Float value) {
        return 4;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Float value, ByteBuffer buffer) {
        buffer.putFloat(value == null ? 0 : value);
    }

    @Override
    public Float deserialize(ByteBuffer buffer) {
        return buffer.getFloat();
    }
}
