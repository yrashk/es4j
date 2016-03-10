package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class CharacterTypeHandler implements TypeHandler<Character> {

    private static final Optional<Integer> SIZE = Optional.of(2);

    @Override
    public byte[] getFingerprint() {
        return new byte[]{7};
    }

    @Override
    public int size(Character value) {
        return 2;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Character value, ByteBuffer buffer) {
        buffer.putChar(value == null ? 0 : value);
    }

    @Override
    public Character deserialize(ByteBuffer buffer) {
        return buffer.getChar();
    }
}
