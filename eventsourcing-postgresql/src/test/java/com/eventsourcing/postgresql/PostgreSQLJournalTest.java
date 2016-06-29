/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.*;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.repository.Journal;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.postgresql.ds.PGSimpleDataSource;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.testng.Assert.*;

@Test
public class PostgreSQLJournalTest extends JournalTest<PostgreSQLJournal> {

    private static DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost/eventsourcing?user=eventsourcing&password=eventsourcing");

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(30);
        config.setDataSource(dataSource);

        return new HikariDataSource(config);
    }

    public PostgreSQLJournalTest() {
        super(new PostgreSQLJournal(dataSource()));
    }


    @Accessors(fluent = true)
    public static class SomeValue {
        @Getter @Setter
        private String value;
    }

    @Accessors(fluent = true)
    public static class TestBean {

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


    public static class SerializationEvent extends StandardEvent {
        @Getter @Setter
        TestBean test;
    }
    public static class SerializationCommand extends StandardCommand<UUID, SerializationEvent> {

        private TestBean t;

        public SerializationCommand() {
        }

        public SerializationCommand(TestBean t) {
            this.t = t;
        }

        @Override public EventStream<SerializationEvent> events(Repository repository) throws Exception {
            SerializationEvent serializationEvent = new SerializationEvent();
            if (t != null) {
                serializationEvent.setTest(t);
            }
            return EventStream.ofWithState(serializationEvent, serializationEvent);
        }

        @Override public UUID onCompletion(SerializationEvent state) {
            return state.uuid();
        }
    }

    @Test @SneakyThrows
    public void serializationNull() {

        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        final SerializationEvent[] serializationEvent = new SerializationEvent[1];

        journal.journal(new SerializationCommand().timestamp(timestamp), new Journal.Listener() {
            @Override public void onCommandStateReceived(Object state) {
                serializationEvent[0] = (SerializationEvent) state;
            }
        });

        Optional<SerializationEvent> event = journal.get(serializationEvent[0].uuid());
        TestBean test = event.get().getTest();

        assertEquals(test.pByte, 0);
        assertEquals(test.oByte, Byte.valueOf((byte) 0));

        assertEquals(test.pByteArr.length, 0);
        assertEquals(test.oByteArr.length, 0);

        assertEquals(test.pShort, 0);
        assertEquals(test.oShort, Short.valueOf((short) 0));

        assertEquals(test.pInt, 0);
        assertEquals(test.oInt, Integer.valueOf(0));

        assertEquals(test.pLong, 0);
        assertEquals(test.oLong, Long.valueOf(0));

        assertTrue(test.pFloat == 0.0);
        assertEquals(test.oFloat, Float.valueOf((float) 0.0));

        assertEquals(test.pDouble, 0.0);
        assertEquals(test.oDouble, Double.valueOf(0.0));


        assertEquals(test.pBoolean, false);
        assertEquals(test.oBoolean, Boolean.FALSE);

        assertEquals(test.str, "");

        assertEquals(test.uuid, new UUID(0,0));

        assertEquals(test.e, TestBean.E.A);

        assertNotNull(test.value);
        assertEquals(test.value.value, "");

        assertNotNull(test.list);
        assertEquals(test.list.size(), 0);

        assertNotNull(test.optional);
        assertFalse(test.optional.isPresent());

        assertNotNull(test.bigDecimal);
        assertEquals(test.bigDecimal, BigDecimal.ZERO);

        assertNotNull(test.date);
        assertEquals(test.date, new Date(0));

    }

    @Test @SneakyThrows
    public void serializationValue() {
        assertEquals(serializationResult(new TestBean().pByte(Byte.MIN_VALUE)).pByte(), Byte.MIN_VALUE);
        assertEquals(serializationResult(new TestBean().pByte(Byte.MAX_VALUE)).pByte(), Byte.MAX_VALUE);

        assertEquals((byte)serializationResult(new TestBean().oByte(Byte.MIN_VALUE)).oByte(), Byte.MIN_VALUE);
        assertEquals((byte)serializationResult(new TestBean().oByte(Byte.MAX_VALUE)).oByte(), Byte.MAX_VALUE);

        assertEquals(serializationResult(new TestBean().pByteArr("Hello, world".getBytes())).pByteArr(),
                     "Hello, world".getBytes());
        assertEquals(serializationResult(new TestBean().oByteArr(toObject(("Hello, world").getBytes()))).oByteArr(),
                     "Hello, world".getBytes());

        assertEquals(serializationResult(new TestBean().pShort(Short.MIN_VALUE)).pShort(), Short.MIN_VALUE);
        assertEquals((short)serializationResult(new TestBean().oShort(Short.MAX_VALUE)).oShort(), Short.MAX_VALUE);

        assertEquals(serializationResult(new TestBean().pInt(Integer.MIN_VALUE)).pInt(), Integer.MIN_VALUE);
        assertEquals((int)serializationResult(new TestBean().oInt(Integer.MAX_VALUE)).oInt(), Integer.MAX_VALUE);

        assertEquals(serializationResult(new TestBean().pLong(Long.MIN_VALUE)).pLong(), Long.MIN_VALUE);
        assertEquals((long)serializationResult(new TestBean().oLong(Long.MAX_VALUE)).oLong(), Long.MAX_VALUE);

        assertEquals(serializationResult(new TestBean().pFloat(Float.MIN_VALUE)).pFloat(), Float.MIN_VALUE);
        assertEquals(serializationResult(new TestBean().oFloat(Float.MAX_VALUE)).oFloat(), Float.MAX_VALUE);

        assertEquals(serializationResult(new TestBean().pDouble(Double.MIN_VALUE)).pDouble(), Double.MIN_VALUE);
        assertEquals(serializationResult(new TestBean().oDouble(Double.MAX_VALUE)).oDouble(), Double.MAX_VALUE);

        assertEquals(serializationResult(new TestBean().pBoolean(true)).pBoolean(), true);
        assertEquals(serializationResult(new TestBean().pBoolean(false)).pBoolean(), false);

        assertEquals((boolean)serializationResult(new TestBean().oBoolean(true)).oBoolean(), true);
        assertEquals((boolean)serializationResult(new TestBean().oBoolean(false)).oBoolean(), false);

        assertEquals(serializationResult(new TestBean().str("Hello, world")).str(), "Hello, world");

        UUID uuid = UUID.randomUUID();
        assertEquals(serializationResult(new TestBean().uuid(uuid)).uuid(), uuid);

        assertEquals(serializationResult(new TestBean().e(TestBean.E.B)).e(), TestBean.E.B);

        assertEquals(serializationResult(new TestBean().value(new SomeValue().value("test"))).value().value(), "test");

        ArrayList<List<String>> l = new ArrayList<>();
        ArrayList<String> l1 = new ArrayList<>();
        l1.add("test");
        l.add(l1);
        assertEquals(serializationResult(new TestBean().list(l)).list().get(0).get(0), "test");


        assertFalse(serializationResult(new TestBean().optional(Optional.empty())).optional().isPresent());
        assertTrue(serializationResult(new TestBean().optional(Optional.of("test"))).optional().isPresent());
        assertEquals(serializationResult(new TestBean().optional(Optional.of("test"))).optional().get(), "test");

        BigDecimal bigDecimal = new BigDecimal("0.00000000000000000000000000001");
        assertEquals(serializationResult(new TestBean().bigDecimal(bigDecimal)).bigDecimal(), bigDecimal);

        Date date = new Date();
        assertEquals(serializationResult(new TestBean().date(date)).date(), date);
    }

    @SneakyThrows
    private TestBean serializationResult(TestBean t) {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        final SerializationEvent[] serializationEvent = new SerializationEvent[1];

        journal.journal(new SerializationCommand(t).timestamp(timestamp), new Journal.Listener() {
            @Override public void onCommandStateReceived(Object state) {
                serializationEvent[0] = (SerializationEvent) state;
            }
        });

        Optional<SerializationEvent> event = journal.get(serializationEvent[0].uuid());
        return event.get().getTest();
    }
}
