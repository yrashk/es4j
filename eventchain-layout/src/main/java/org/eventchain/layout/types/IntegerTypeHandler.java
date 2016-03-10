package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class IntegerTypeHandler implements TypeHandler<Integer> {

    private static final Optional<Integer> SIZE = Optional.of(4);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{2};
    }

    @Override
    public int size(Integer value) {
        return 4;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Integer value, ByteBuffer buffer) {
        buffer.putInt(value == null ? 0 : value);
    }

    @Override
    public Integer deserialize(ByteBuffer buffer) {
        return buffer.getInt();
    }
}
