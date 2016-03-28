/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain.hlc;

import com.google.common.util.concurrent.AbstractService;
import lombok.SneakyThrows;
import org.eventchain.layout.Deserializer;
import org.eventchain.layout.Layout;
import org.eventchain.layout.Serializer;
import org.eventchain.layout.TypeHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HybridTimestampTest {

    class TestPhysicalTimeProvider extends AbstractService implements PhysicalTimeProvider {

        private long physicalTime = 0;

        @Override
        public long getPhysicalTime() {
            return physicalTime;
        }

        public void setPhysicalTime(long physicalTime) {
            this.physicalTime = physicalTime;
        }

        @Override
        protected void doStart() {
            notifyStarted();
        }

        @Override
        protected void doStop() {
            notifyStopped();
        }
    };
    private TestPhysicalTimeProvider physicalTimeProvider;

    @BeforeClass
    public void setup() {
        physicalTimeProvider = new TestPhysicalTimeProvider();
    }

    @Test
    public void testTimestamp() {
        HybridTimestamp timestamp = new HybridTimestamp(physicalTimeProvider);
        timestamp.update();
        timestamp.update();
        assertTrue(timestamp.getLogicalCounter() > 0);
        byte[] ts = timestamp.getByteArray();
        HybridTimestamp timestamp1 = new HybridTimestamp(physicalTimeProvider, ts);
        assertEquals(timestamp1.getByteArray(), ts);
        assertEquals(timestamp1.getLogicalCounter(), timestamp.getLogicalCounter());
    }

    @Test @SneakyThrows
    public void layout() {
        HybridTimestamp timestamp = new HybridTimestamp(physicalTimeProvider);
        timestamp.update();

        Layout<HybridTimestamp> layout = new Layout<>(HybridTimestamp.class);
        Serializer<HybridTimestamp> serializer = new Serializer<>(layout);
        Deserializer<HybridTimestamp> deserializer = new Deserializer<>(layout);
        assertEquals(serializer.size(timestamp), 16 + TypeHandler.SIZE_TAG_LENGTH);

        ByteBuffer buffer = serializer.serialize(timestamp);
        buffer.rewind();

        HybridTimestamp timestamp1 = deserializer.deserialize(buffer);

        assertEquals(timestamp1.compareTo(timestamp), 0);
    }

    @Test
    public void test() {
        long ts, ts_;
        HybridTimestamp timestamp = new HybridTimestamp(physicalTimeProvider);

        ts = (long)1 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update();
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        // clock didn't move
        ts = (long)1 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update();
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(1, timestamp.getLogicalCounter());

        // clock moved back
        ts = (long)0 << 32 | 1;
        physicalTimeProvider.setPhysicalTime(ts);
        ts_ = timestamp.getLogicalTime();
        timestamp.update();
        assertEquals(ts_, timestamp.getLogicalTime());
        assertEquals(2, timestamp.getLogicalCounter());

        // clock moved ahead
        ts = (long)2 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update();
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        // event happens, but wall ahead
        ts = (long)3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)1 << 32 | 2, 3);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        // event happens, wall ahead but unchanged
        ts = (long)3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)1 << 32 | 2, 3);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(1, timestamp.getLogicalCounter());

        //  event happens at wall, which is still unchanged
        ts = (long)3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)3 << 32 | 0, 1);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(2, timestamp.getLogicalCounter());

        //  event with larger logical, wall unchaged
        ts = (long)3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)3 << 32 | 0, 99);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(100, timestamp.getLogicalCounter());

        // event with larger wall, our wall behind
        ts = (long)3 << 32 | 5;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)4 << 32 | 4, 100);
        assertEquals((long) 4 << 32 | 4, timestamp.getLogicalTime());
        assertEquals(101, timestamp.getLogicalCounter());

        // event behind wall, but ahead of previous state
        ts = (long)5 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)4 << 32 | 5, 0);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        ts = (long)4 << 32 | 9;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)5 << 32 | 0, 99);
        assertEquals((long) 5 << 32 | 0, timestamp.getLogicalTime());
        assertEquals(100, timestamp.getLogicalCounter());

        // event at state, lower logical than state
        ts = (long)0 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long)5 << 32 | 0, 50);
        assertEquals((long) 5 << 32 | 0, timestamp.getLogicalTime());
        assertEquals(101, timestamp.getLogicalCounter());

        // another update API
        timestamp.update(timestamp);
        assertEquals((long) 5 << 32 | 0, timestamp.getLogicalTime());
        assertEquals(102, timestamp.getLogicalCounter());

    }

}