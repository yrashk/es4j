package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class DoubleTypeHandler implements TypeHandler<Double> {

    private static Optional<Integer> SIZE = Optional.of(8);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{5};
    }

    @Override
    public int size(Double value) {
        return 8;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Double value, ByteBuffer buffer) {
        buffer.putDouble(value == null ? 0 : value);
    }

    @Override
    public Double deserialize(ByteBuffer buffer) {
        return buffer.getDouble();
    }
}
