/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;

import static org.testng.Assert.*;

public class SerializerTest {

    public static final String PI = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196442881097566593344612847564823378678316527120190914564856692346034861045432664821339360726024914127372458700660631558817488152092096282925409171536436789259036001133053054882046652138414695194151160943305727036575959195309218611738193261179310511854807446237996274956735188575272489122793818301194912983367336244065664308602139494639522473719070217986094370277053921717629317675238467481846766940513200056812714526356082778577134275778960917363717872146844090122495343014654958537105079227968925892354201995611212902196086403441815981362977477130996051870721134999999837297804995105973173281609631859502445945534690830264252230825334468503526193118817101000313783875288658753320838142061717766914730359825349042875546873115956286388235378759375195778185778053217122680661300192787661119590921642019";
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
        private String str;

        @Getter @Setter
        private UUID uuid;

        public enum E {A, B}

        ;
        @Getter @Setter
        private E e;

        @Getter @Setter
        private SomeValue value;

        @Getter @Setter
        private List<List<String>> list;

        @Getter @Setter
        private Optional<String> optional;

        @Getter @Setter
        private BigDecimal bigDecimal;

        @Getter @Setter
        private Date date;
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

        assertEquals(deserialized.getValue().value(),
                     ""); // it is an empty string because we don't preserve String nullity
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


    @Test
    public void bigDecimalSerialization() {
        TestBean test = new TestBean();
        test.setBigDecimal(new BigDecimal(PI));

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getBigDecimal(), new BigDecimal(PI));
    }

    @Test
    public void dateSerialization() {
        TestBean test = new TestBean();
        Date date = new Date();
        test.setDate(date);

        ByteBuffer buffer = serializer.serialize(test);

        buffer.rewind();
        TestBean deserialized = new TestBean();
        deserializer.deserialize(deserialized, buffer);

        assertEquals(deserialized.getDate(), date);
    }
}