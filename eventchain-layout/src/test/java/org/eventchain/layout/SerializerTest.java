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
package org.eventchain.layout;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.testng.Assert.*;

public class SerializerTest {

    private Layout<TestBean> layout;
    private Serializer<TestBean> serializer;
    private Deserializer<TestBean> deserializer;

    @Accessors(fluent = true)
    public static class SomeValue {
        @Getter @Setter
        private String value;
    }

    private static class TestBean {

        @Getter @Setter
        private byte pByte;
        @Getter @Setter
        private Byte oByte;

        @Getter @Setter
        private byte[] pByteArr;
        @Getter @Setter
        private Byte[] oByteArr;

        @Getter @Setter
        private short pShort;
        @Getter @Setter
        private Short oShort;

        @Getter @Setter
        private int pInt;
        @Getter @Setter
        private Integer oInt;

        @Getter @Setter
        private long pLong;
        @Getter @Setter
        private Long oLong;

        @Getter @Setter
        private float pFloat;
        @Getter @Setter
        private Float oFloat;

        @Getter @Setter
        private double pDouble;
        @Getter @Setter
        private Double oDouble;

        @Getter @Setter
        private boolean pBoolean;
        @Getter @Setter
        private Boolean oBoolean;

        @Getter @Setter
        private char pChar;
        @Getter @Setter
        private Character oChar;

        @Getter @Setter
        private String str;

        @Getter @Setter
        private UUID uuid;

        public enum E { A, B };
        @Getter @Setter
        private E e;

        @Getter @Setter
        private SomeValue value;

        @Getter @Setter
        private List<List<String>> list;

        @Getter @Setter
        private Optional<String> optional;

    }

    @BeforeClass
    @SneakyThrows
    public void setUp() {
        layout = new Layout<>(TestBean.class);
        serializer = new Serializer<>(layout);
        deserializer = new Deserializer<>(layout);
    }

    @Test
    public void byteSerialization() {
        TestBean test = new TestBean();
        test.setPByte(Byte.MIN_VALUE);
        test.setOByte(Byte.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPByte(), Byte.MIN_VALUE);
        assertEquals(deserialized.getOByte(), (Byte) Byte.MAX_VALUE);
    }

    @Test
    public void byteArraySerialization() {
        TestBean test = new TestBean();
        test.setPByteArr(new byte[]{Byte.MIN_VALUE});
        test.setOByteArr(new Byte[]{Byte.MAX_VALUE});

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPByteArr(), new byte[]{Byte.MIN_VALUE});
        assertEquals(deserialized.getOByteArr(), new Byte[]{Byte.MAX_VALUE});
    }

    @Test
    public void shortSerialization() {
        TestBean test = new TestBean();
        test.setPShort(Short.MIN_VALUE);
        test.setOShort(Short.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPShort(), Short.MIN_VALUE);
        assertEquals(deserialized.getOShort(), (Short) Short.MAX_VALUE);
    }

    @Test
    public void intSerialization() {
        TestBean test = new TestBean();
        test.setPInt(Integer.MIN_VALUE);
        test.setOInt(Integer.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPInt(), Integer.MIN_VALUE);
        assertEquals(deserialized.getOInt(), (Integer) Integer.MAX_VALUE);
    }

    @Test
    public void longSerialization() {
        TestBean test = new TestBean();
        test.setPLong(Long.MIN_VALUE);
        test.setOLong(Long.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPLong(), Long.MIN_VALUE);
        assertEquals(deserialized.getOLong(), (Long) Long.MAX_VALUE);
    }

    @Test
    public void floatSerialization() {
        TestBean test = new TestBean();
        test.setPFloat(Float.MIN_VALUE);
        test.setOFloat(Float.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPFloat(), Float.MIN_VALUE);
        assertEquals(deserialized.getOFloat(), Float.MAX_VALUE);
    }

    @Test
    public void doubleSerialization() {
        TestBean test = new TestBean();
        test.setPDouble(Double.MIN_VALUE);
        test.setODouble(Double.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPDouble(), Double.MIN_VALUE);
        assertEquals(deserialized.getODouble(), Double.MAX_VALUE);
    }

    @Test
    public void booleanSerialization() {
        TestBean test = new TestBean();
        test.setPBoolean(true);
        test.setOBoolean(false);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.isPBoolean(), true);
        assertEquals(deserialized.getOBoolean(), (Boolean) false);
    }

    @Test
    public void charSerialization() {
        TestBean test = new TestBean();
        test.setPChar(Character.MIN_VALUE);
        test.setOChar(Character.MAX_VALUE);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getPChar(), Character.MIN_VALUE);
        assertEquals(deserialized.getOChar(), (Character) Character.MAX_VALUE);
    }

    @Test
    public void stringSerialization() {
        TestBean test = new TestBean();
        test.setStr("test");

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getStr(), "test");
    }

    @Test
    public void uuidSerialization() {
        TestBean test = new TestBean();
        UUID uuid = UUID.randomUUID();
        test.setUuid(uuid);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getUuid(), uuid);
    }

    @Test
    public void enumSerialization() {
        TestBean test = new TestBean();
        test.setE(TestBean.E.A);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getE(), TestBean.E.A);
    }

    @Test
    public void layoutSerialization() {
        TestBean test = new TestBean();
        test.setValue(new SomeValue().value("test"));

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getValue().value(), "test");
    }

    @Test
    public void nullLayoutSerialization() {
        TestBean test = new TestBean();

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getValue().value(), ""); // it is an empty string because we don't preserve String nullity
    }


    @Test
    public void listSerialization() {
        TestBean test = new TestBean();
        LinkedList<List<String>> list = new LinkedList<>();
        list.add(new LinkedList<>(Arrays.asList("Hello")));
        test.setList(list);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getList().get(0).get(0), "Hello");
    }

    @Test
    public void optionalSerialization() {
        TestBean test = new TestBean();
        assertNull(test.getOptional());
        ByteBuffer buffer = serializer.serialize(test);
        TestBean deserialized = new TestBean();
        buffer.rewind();
        deserializer.deserialize(deserialized, buffer);
        assertFalse(deserialized.getOptional().isPresent());

        test.setOptional(Optional.empty());

        buffer = serializer.serialize(test);

        buffer.rewind();
        deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertFalse(deserialized.getOptional().isPresent());

        test.setOptional(Optional.of("hello"));

        buffer = serializer.serialize(test);

        buffer.rewind();
        deserializer.deserialize(deserialized, buffer);

        assertTrue(deserialized.getOptional().isPresent());
        assertEquals(deserialized.getOptional().get(), "hello");
    }
}