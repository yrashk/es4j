package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;

public class StringTypeHandler implements TypeHandler<String> {
    @Override
    public byte[] getFingerprint() {
        return new byte[]{8};
    }

    @Override
    public int size(String value) {
        return value == null ? 0 : value.length();
    }

    @Override
    public void serialize(String value, ByteBuffer buffer) {
        buffer.putInt(size(value));
        if (value != null) {
            buffer.put(value.getBytes());
        }
    }

    @Override
    public String deserialize(ByteBuffer buffer) {
        int len = buffer.getInt();
        byte[] buf = new byte[len];
        buffer.get(buf);
        return new String(buf);
    }
}
