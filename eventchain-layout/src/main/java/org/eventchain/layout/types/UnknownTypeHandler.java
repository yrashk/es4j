package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;

public class UnknownTypeHandler implements TypeHandler {

    @Override
    public byte[] getFingerprint() {
        return new byte[0];
    }

    @Override
    public int size(Object value) {
        return 0;
    }

    @Override
    public void serialize(Object value, ByteBuffer buffer) {
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        return null;
    }
}
