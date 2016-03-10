package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class LongTypeHandler implements TypeHandler<Long> {

    private static final Optional<Integer> SIZE = Optional.of(8);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{3};
    }

    @Override
    public int size(Long value) {
        return 8;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }


    @Override
    public void serialize(Long value, ByteBuffer buffer) {
        buffer.putLong(value == null ? 0 : value);
    }

    @Override
    public Long deserialize(ByteBuffer buffer) {
        return buffer.getLong();
    }
}
