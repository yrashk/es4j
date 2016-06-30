/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.eventsourcing.layout.binary.BinarySerialization;
import lombok.*;
import lombok.experimental.Accessors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;

import static org.testng.Assert.*;

public class SerializerTest {

    public static final String PI = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196442881097566593344612847564823378678316527120190914564856692346034861045432664821339360726024914127372458700660631558817488152092096282925409171536436789259036001133053054882046652138414695194151160943305727036575959195309218611738193261179310511854807446237996274956735188575272489122793818301194912983367336244065664308602139494639522473719070217986094370277053921717629317675238467481846766940513200056812714526356082778577134275778960917363717872146844090122495343014654958537105079227968925892354201995611212902196086403441815981362977477130996051870721134999999837297804995105973173281609631859502445945534690830264252230825334468503526193118817101000313783875288658753320838142061717766914730359825349042875546873115956286388235378759375195778185778053217122680661300192787661119590921642019";
    private Layout<TestClass> layout;
    private ObjectSerializer<TestClass> serializer;
    private ObjectDeserializer<TestClass> deserializer;

    @Value
    @Accessors(fluent = true)
    public static class SomeValue {
        private final String value;
    }

    @Value @Builder
    public static class TestClass {
        private final byte pByte;
        private final Byte oByte;

        private final byte[] pByteArr;
        private final Byte[] oByteArr;

        private final short pShort;
        private final Short oShort;

        private final int pInt;
        private final Integer oInt;

        private final long pLong;
        private final Long oLong;

        private final float pFloat;
        private final Float oFloat;

        private final double pDouble;
        private final Double oDouble;

        private final boolean pBoolean;
        private final Boolean oBoolean;

        private final String str;

        private final UUID uuid;

        public enum E {A, B}

        private final E e;

        private final SomeValue value;

        private final List<List<String>> list;

        private final Optional<String> optional;

        private final BigDecimal bigDecimal;

        private final Date date;

        public TestClass(byte pByte, Byte oByte, byte[] pByteArr, Byte[] oByteArr, short pShort, Short oShort, int pInt,
                         Integer oInt, long pLong, Long oLong, float pFloat, Float oFloat, double pDouble,
                         Double oDouble, boolean pBoolean, Boolean oBoolean, String str, UUID uuid,
                         E e, SomeValue value, List<List<String>> list, Optional<String> optional, BigDecimal bigDecimal,
                         Date date) {
            this.pByte = pByte;
            this.oByte = oByte;
            this.pByteArr = pByteArr;
            this.oByteArr = oByteArr;
            this.pShort = pShort;
            this.oShort = oShort;
            this.pInt = pInt;
            this.oInt = oInt;
            this.pLong = pLong;
            this.oLong = oLong;
            this.pFloat = pFloat;
            this.oFloat = oFloat;
            this.pDouble = pDouble;
            this.oDouble = oDouble;
            this.pBoolean = pBoolean;
            this.oBoolean = oBoolean;
            this.str = str;
            this.uuid = uuid;
            this.e = e;
            this.value = value;
            this.list = list;
            this.optional = optional;
            this.bigDecimal = bigDecimal;
            this.date = date;
        }
    }

    @BeforeClass
    @SneakyThrows
    public void setUp() {
        layout = Layout.forClass(TestClass.class);
        BinarySerialization serialization = BinarySerialization.getInstance();
        serializer = serialization.getSerializer(TestClass.class);
        deserializer = serialization.getDeserializer(TestClass.class);
        assertTrue(layout.getProperties().size() > 0);
    }

    @Test
    public void byteSerialization() {
        TestClass test = TestClass.builder().pByte(Byte.MIN_VALUE).oByte(Byte.MAX_VALUE).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPByte(), Byte.MIN_VALUE);
        assertEquals(deserialized.getOByte(), (Byte) Byte.MAX_VALUE);
    }

    @Test
    public void byteArraySerialization() {

        TestClass test = TestClass.builder().pByteArr(new byte[]{Byte.MIN_VALUE}).oByteArr(new Byte[]{Byte.MAX_VALUE})
                                  .build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPByteArr(), new byte[]{Byte.MIN_VALUE});
        assertEquals(deserialized.getOByteArr(), new Byte[]{Byte.MAX_VALUE});
    }

    @Test
    public void shortSerialization() {
        TestClass test = TestClass.builder().pShort(Short.MIN_VALUE).oShort(Short.MAX_VALUE).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPShort(), Short.MIN_VALUE);
        assertEquals(deserialized.getOShort(), (Short) Short.MAX_VALUE);
    }

    @Test
    public void intSerialization() {
        TestClass test = TestClass.builder().pInt(Integer.MIN_VALUE).oInt(Integer.MAX_VALUE).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPInt(), Integer.MIN_VALUE);
        assertEquals(deserialized.getOInt(), (Integer) Integer.MAX_VALUE);
    }

    @Test
    public void longSerialization() {
        TestClass test = TestClass.builder().pLong(Long.MIN_VALUE).oLong(Long.MAX_VALUE).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPLong(), Long.MIN_VALUE);
        assertEquals(deserialized.getOLong(), (Long) Long.MAX_VALUE);
    }

    @Test
    public void floatSerialization() {
        TestClass test = TestClass.builder().pFloat(Float.MIN_VALUE).oFloat(Float.MAX_VALUE).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPFloat(), Float.MIN_VALUE);
        assertEquals(deserialized.getOFloat(), Float.MAX_VALUE);
    }

    @Test
    public void doubleSerialization() {
        TestClass test = TestClass.builder().pDouble(Double.MIN_VALUE).oDouble(Double.MAX_VALUE).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getPDouble(), Double.MIN_VALUE);
        assertEquals(deserialized.getODouble(), Double.MAX_VALUE);
    }

    @Test
    public void booleanSerialization() {
        TestClass test = TestClass.builder().pBoolean(true).oBoolean(false).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.isPBoolean(), true);
        assertEquals(deserialized.getOBoolean(), (Boolean) false);
    }

    @Test
    public void stringSerialization() {
        TestClass test = TestClass.builder().str("test").build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getStr(), "test");
    }

    @Test
    public void uuidSerialization() {
        UUID uuid = UUID.randomUUID();
        TestClass test = TestClass.builder().uuid(uuid).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getUuid(), uuid);
    }

    @Test
    public void enumSerialization() {
        TestClass test = TestClass.builder().e(TestClass.E.A).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getE(), TestClass.E.A);
    }

    @Test
    public void layoutSerialization() {
        TestClass test = TestClass.builder().value(new SomeValue("test")).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getValue().value(), "test");
    }

    @Test
    public void nullLayoutSerialization() {
        TestClass test = TestClass.builder().build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getValue().value(),
                     ""); // it is an empty string because we don't preserve String nullity
    }


    @Test
    public void listSerialization() {
        LinkedList<List<String>> list = new LinkedList<>();
        list.add(new LinkedList<>(Arrays.asList("Hello")));
        TestClass test = TestClass.builder().list(list).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getList().get(0).get(0), "Hello");
    }

    @Test
    public void optionalSerialization() {
        TestClass test = TestClass.builder().build();
        assertNull(test.getOptional());

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);
        assertFalse(deserialized.getOptional().isPresent());

        test = TestClass.builder().optional(Optional.empty()).build();

        buffer = serializer.serialize(test);
        buffer.rewind();

        deserialized = deserializer.deserialize(buffer);
        assertFalse(deserialized.getOptional().isPresent());

        test = TestClass.builder().optional(Optional.of("hello")).build();

        buffer = serializer.serialize(test);
        buffer.rewind();

        deserialized = deserializer.deserialize(buffer);

        assertTrue(deserialized.getOptional().isPresent());
        assertEquals(deserialized.getOptional().get(), "hello");
    }


    @Test
    public void bigDecimalSerialization() {
        TestClass test = TestClass.builder().bigDecimal(new BigDecimal(PI)).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getBigDecimal(), new BigDecimal(PI));
    }

    @Test
    public void dateSerialization() {
        Date date = new Date();
        TestClass test = TestClass.builder().date(date).build();

        ByteBuffer buffer = serializer.serialize(test);
        buffer.rewind();

        TestClass deserialized = deserializer.deserialize(buffer);

        assertEquals(deserialized.getDate(), date);
    }
}