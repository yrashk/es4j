/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

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

    @Test(expectedExceptions = Deserializer.NoEmptyConstructorException.class)
    @SneakyThrows
    public void noEmptyConstuctor() {
        new Deserializer<>(new Layout<>(NoEmptyConstructor.class));
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
        Serializer<MatchingConstructor> serializer = new Serializer<>(layout);
        Deserializer<MatchingConstructor> deserializer = new Deserializer<>(layout);
        ByteBuffer buffer = serializer.serialize(new MatchingConstructor("test1", "test2"));
        buffer.rewind();
        MatchingConstructor value = deserializer.deserialize(buffer);
        assertEquals(value.getTest1(), "test1");
        assertEquals(value.getTest2(), "test2");
    }

    private static class ReadonlyTest {
        @Getter
        private String getter;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SneakyThrows
    public void readonly() {
        Layout<ReadonlyTest> layout = new Layout<>(ReadonlyTest.class, true);
        assertTrue(layout.isReadOnly());
        new Deserializer<>(layout);
    }


}