/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import org.h2.mvstore.WriteBuffer;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.testng.Assert.assertEquals;

public class ByteBufferDataTypeTest {
    @Test
    public void testCompare() throws Exception {
        ByteBuffer b1 = ByteBuffer.allocate(1);
        ByteBuffer b2 = ByteBuffer.allocate(1);
        b1.put((byte) 0);
        b2.put((byte) 1);
        assertEquals(new ByteBufferDataType().compare(b1, b2), b1.compareTo(b2));
    }

    @Test
    public void testGetMemory() throws Exception {
        ByteBuffer b = ByteBuffer.allocate(100);
        b.position(1);
        b.limit(50);
        assertEquals(new ByteBufferDataType().getMemory(b), 49);
    }

    @Test
    public void testWrite() throws Exception {
        ByteBuffer b = ByteBuffer.allocate(100);
        b.position(1);
        b.limit(50);
        b.putInt(100);
        b.position(1);
        WriteBuffer writeBuffer = new WriteBuffer();
        ByteBufferDataType byteBufferDataType = new ByteBufferDataType();
        byteBufferDataType.write(writeBuffer, b);
        ByteBuffer rb = writeBuffer.getBuffer();
        rb.rewind();
        ByteBuffer b1 = (ByteBuffer) byteBufferDataType.read(rb);
        assertEquals(b1.limit(), 49);
        assertEquals(b1.getInt(), 100);

        rb.rewind();

        ByteBuffer[] byteBuffers = new ByteBuffer[1];
        byteBufferDataType.read(rb, byteBuffers, 1, false);

        assertEquals(byteBuffers[0].getInt(), 100);
    }

}