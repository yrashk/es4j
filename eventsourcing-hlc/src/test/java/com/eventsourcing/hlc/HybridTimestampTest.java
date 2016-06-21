/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.hlc;

import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.Property;
import com.eventsourcing.layout.Serializer;
import com.google.common.util.concurrent.AbstractService;
import lombok.SneakyThrows;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.List;

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
    }

    ;
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
        HybridTimestamp timestamp1 = new HybridTimestamp(physicalTimeProvider, timestamp.getLogicalTime(),
                                                         timestamp.getLogicalCounter());
        assertEquals(timestamp1.getLogicalCounter(), timestamp.getLogicalCounter());
    }

    @Test @SneakyThrows
    public void layout() {
        Layout<HybridTimestamp> layout = new Layout<>(HybridTimestamp.class);
        List<Property<HybridTimestamp>> properties = layout.getProperties();
        assertEquals(properties.size(), 2);
        assertTrue(properties.stream().anyMatch(p -> p.getName().contentEquals("logicalTime")));
        assertTrue(properties.stream().anyMatch(p -> p.getName().contentEquals("logicalCounter")));

        HybridTimestamp timestamp = new HybridTimestamp(physicalTimeProvider);
        timestamp.update();

        Serializer<HybridTimestamp> serializer = new Serializer<>(layout);
        Deserializer<HybridTimestamp> deserializer = new Deserializer<>(layout);

        ByteBuffer buffer = serializer.serialize(timestamp);
        buffer.rewind();

        HybridTimestamp timestamp1 = deserializer.deserialize(buffer);

        assertEquals(timestamp1.compareTo(timestamp), 0);
    }

    @Test
    public void test() {
        long ts, ts_;
        HybridTimestamp timestamp = new HybridTimestamp(physicalTimeProvider);

        ts = (long) 1 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update();
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        // clock didn't move
        ts = (long) 1 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update();
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(1, timestamp.getLogicalCounter());

        // clock moved back
        ts = (long) 0 << 32 | 1;
        physicalTimeProvider.setPhysicalTime(ts);
        ts_ = timestamp.getLogicalTime();
        timestamp.update();
        assertEquals(ts_, timestamp.getLogicalTime());
        assertEquals(2, timestamp.getLogicalCounter());

        // clock moved ahead
        ts = (long) 2 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update();
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        // event happens, but wall ahead
        ts = (long) 3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 1 << 32 | 2, 3);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        // event happens, wall ahead but unchanged
        ts = (long) 3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 1 << 32 | 2, 3);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(1, timestamp.getLogicalCounter());

        //  event happens at wall, which is still unchanged
        ts = (long) 3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 3 << 32 | 0, 1);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(2, timestamp.getLogicalCounter());

        //  event with larger logical, wall unchaged
        ts = (long) 3 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 3 << 32 | 0, 99);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(100, timestamp.getLogicalCounter());

        // event with larger wall, our wall behind
        ts = (long) 3 << 32 | 5;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 4 << 32 | 4, 100);
        assertEquals((long) 4 << 32 | 4, timestamp.getLogicalTime());
        assertEquals(101, timestamp.getLogicalCounter());

        // event behind wall, but ahead of previous state
        ts = (long) 5 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 4 << 32 | 5, 0);
        assertEquals(ts, timestamp.getLogicalTime());
        assertEquals(0, timestamp.getLogicalCounter());

        ts = (long) 4 << 32 | 9;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 5 << 32 | 0, 99);
        assertEquals((long) 5 << 32 | 0, timestamp.getLogicalTime());
        assertEquals(100, timestamp.getLogicalCounter());

        // event at state, lower logical than state
        ts = (long) 0 << 32 | 0;
        physicalTimeProvider.setPhysicalTime(ts);
        timestamp.update((long) 5 << 32 | 0, 50);
        assertEquals((long) 5 << 32 | 0, timestamp.getLogicalTime());
        assertEquals(101, timestamp.getLogicalCounter());

        // another update API
        timestamp.update(timestamp);
        assertEquals((long) 5 << 32 | 0, timestamp.getLogicalTime());
        assertEquals(102, timestamp.getLogicalCounter());

    }

}