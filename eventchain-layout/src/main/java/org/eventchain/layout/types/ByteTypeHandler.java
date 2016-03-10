package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class ByteTypeHandler implements TypeHandler<Byte> {

    private static final Optional<Integer> SIZE = Optional.of(1);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{0};
    }

    @Override
    public int size(Byte value) {
        return 1;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Byte value, ByteBuffer buffer) {
        buffer.put(value == null ? 0 : value);
    }

    @Override
    public Byte deserialize(ByteBuffer buffer) {
        return buffer.get();
    }
}
