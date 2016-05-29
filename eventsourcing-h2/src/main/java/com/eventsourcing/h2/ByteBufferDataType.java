/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;

import java.nio.ByteBuffer;

public class ByteBufferDataType implements DataType {
    @Override public int compare(Object a, Object b) {
        if (a instanceof ByteBuffer && b instanceof ByteBuffer) {
            return ((ByteBuffer) a).compareTo((ByteBuffer) b);
        } else {
            throw new RuntimeException("ByteBuffers expected");
        }
    }

    @Override public int getMemory(Object obj) {
        return ((ByteBuffer) obj).limit() - ((ByteBuffer) obj).position();
    }

    @Override public void write(WriteBuffer buff, Object obj) {
        ByteBuffer o = (ByteBuffer) obj;
        int sz = o.limit();
        if (sz == 0) {
            sz = o.capacity();
        }
        buff.putInt(sz - o.position());
        buff.put(o);
    }

    @Override public void write(WriteBuffer buff, Object[] obj, int len, boolean key) {
        for (Object o : obj) {
            write(buff, o);
        }
    }

    @Override public Object read(ByteBuffer buff) {
        int sz = buff.getInt();
        return buff.slice().limit(sz);
    }

    @Override public void read(ByteBuffer buff, Object[] obj, int len, boolean key) {
        for (int i = 0; i < obj.length; i++) {
            obj[i] = read(buff);
        }
    }
}
