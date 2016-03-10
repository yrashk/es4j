package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class UUIDTypeHandler implements TypeHandler<UUID> {

    private static final Optional<Integer> SIZE = Optional.of(16);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{9};
    }

    @Override
    public int size(UUID value) {
        return 16;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(UUID value, ByteBuffer buffer) {
        if (value == null) {
            value = new UUID(0, 0);
        }
        buffer.putLong(value.getMostSignificantBits());
        buffer.putLong(value.getLeastSignificantBits());
    }

    @Override
    public UUID deserialize(ByteBuffer buffer) {
        long most = buffer.getLong();
        long least = buffer.getLong();
        return new UUID(most, least);
    }
}
