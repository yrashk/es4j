/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.eventsourcing.layout.binary.BinarySerialization;
import com.eventsourcing.layout.binary.ObjectBinaryDeserializer;
import com.eventsourcing.layout.binary.ObjectBinarySerializer;
import com.eventsourcing.layout.types.ObjectTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.testng.Assert.*;

public class DeserializerTest {

    public class NoEmptyConstructor {
        @Getter @Setter
        private String test;
    }

    @Test(expectedExceptions = ObjectBinaryDeserializer.NoEmptyConstructorException.class)
    @SneakyThrows
    public void noEmptyConstuctor() {
        BinarySerialization serialization = BinarySerialization.getInstance();
        ObjectDeserializer<Object> deserializer = serialization.getDeserializer(NoEmptyConstructor.class);
        deserializer.deserialize(ByteBuffer.allocate(0)); // size doesn't matter, should throw an exception
    }


    @AllArgsConstructor
    public static class MatchingConstructor {
        @Getter
        private String test1;
        @Getter
        private String test2;
    }

    @Test
    @SneakyThrows
    public void matchingConstructor() {
        Layout<MatchingConstructor> layout = new Layout<>(MatchingConstructor.class);
        assertFalse(layout.isReadOnly());
        BinarySerialization serialization = BinarySerialization.getInstance();
        ObjectSerializer<MatchingConstructor> serializer = serialization.getSerializer(MatchingConstructor.class);
        ObjectDeserializer<MatchingConstructor> deserializer = serialization.getDeserializer(MatchingConstructor.class);
        ByteBuffer buffer = serializer.serialize(new MatchingConstructor("test1", "test2"));
        buffer.rewind();
        MatchingConstructor value = deserializer.deserialize(buffer);
        assertEquals(value.getTest1(), "test1");
        assertEquals(value.getTest2(), "test2");
    }

    public static class ReadonlyTest {
        @Getter
        private String getter;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SneakyThrows
    public void readonly() {
        Layout<ReadonlyTest> layout = new Layout<>(ReadonlyTest.class, true);
        assertTrue(layout.isReadOnly());

        BinarySerialization serialization = BinarySerialization.getInstance();
        ObjectDeserializer<Object> deserializer = serialization.getDeserializer(ReadonlyTest.class, true);
        deserializer.deserialize(ByteBuffer.allocate(0)); // size doesn't matter, should throw an exception
    }


}