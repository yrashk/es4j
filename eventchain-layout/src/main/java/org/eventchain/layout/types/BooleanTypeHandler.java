package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class BooleanTypeHandler implements TypeHandler<Boolean> {

    private static final Optional<Integer> SIZE = Optional.of(1);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{6};
    }

    @Override
    public int size(Boolean value) {
        return 1;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Boolean value, ByteBuffer buffer) {
        buffer.put((byte) (value == null ? 0 : (value ? 1 : 0)));
    }

    @Override
    public Boolean deserialize(ByteBuffer buffer) {
        return buffer.get() == 1;
    }
}
