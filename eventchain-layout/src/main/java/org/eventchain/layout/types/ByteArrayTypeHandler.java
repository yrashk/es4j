package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.*;

public class ByteArrayTypeHandler implements TypeHandler {

    private boolean primitive;

    public ByteArrayTypeHandler(boolean primitive) {
        this.primitive = primitive;
    }

    @Override
    public byte[] getFingerprint() {
        return new byte[]{(byte)255, 0};
    }

    @Override
    public int size(Object value) {
        return SIZE_TAG_LENGTH + getPrimitiveArray(value).length;
    }

    @Override
    public void serialize(Object value, ByteBuffer buffer) {
        byte[] bytes = getPrimitiveArray(value);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        if (primitive) {
            return bytes;
        } else {
            return toObject(bytes);
        }
    }

    private byte[] getPrimitiveArray(Object value) {
        if (value instanceof byte[]) {
            return nullToEmpty((byte[])value);
        }
        if (value instanceof Byte[]) {
            return nullToEmpty(toPrimitive((Byte[]) value));
        }
        if (value == null) {
            return new byte[]{};
        }
        throw new IllegalArgumentException(value.toString());
    }

}
