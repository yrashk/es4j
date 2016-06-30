/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.events.EventCausalityEstablished;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public class EventStreamTest {
    @Test
    public void testBuilder() throws Exception {
        EventStream<String> test = EventStream.builder("test").add(EventCausalityEstablished.builder().build()).build();
        assertEquals(test.getState(), "test");
        Event event = test.getStream().findFirst().get();
        assertTrue(event instanceof EventCausalityEstablished);
    }

    @Test
    public void testBuilder1() throws Exception {
        EventStream<String> test = EventStream.<String>builder().add(EventCausalityEstablished.builder().build()).build();
        assertNull(test.getState());
        Event event = test.getStream().findFirst().get();
        assertTrue(event instanceof EventCausalityEstablished);
    }

    @Test
    public void testEmpty() throws Exception {
        EventStream<Object> empty = EventStream.empty();
        assertNull(empty.getState());
        assertFalse(empty.getStream().anyMatch(x -> true));
    }

    @Test
    public void testEmpty1() throws Exception {
        EventStream<Object> empty = EventStream.empty("empty");
        assertEquals(empty.getState(), "empty");
        assertFalse(empty.getStream().anyMatch(x -> true));
    }

    @Test
    public void testOf() throws Exception {
        EventStream<Object> test = EventStream.of(EventCausalityEstablished.builder().build());
        assertNull(test.getState());
        assertEquals(test.getStream().collect(Collectors.<Event>toSet()).size(), 1);
    }

    @Test
    public void testOf1() throws Exception {
        EventStream<String> test = EventStream.of(EventCausalityEstablished.builder().build(),
                                                  EventCausalityEstablished.builder().build());
        assertNull(test.getState());
        assertEquals(test.getStream().collect(Collectors.<Event>toSet()).size(), 2);
    }

    @Test
    public void testOf2() throws Exception {
        EventStream<String> test = EventStream.ofWithState("test", EventCausalityEstablished.builder().build());
        assertEquals(test.getState(), "test");
        assertEquals(test.getStream().collect(Collectors.<Event>toSet()).size(), 1);
    }

    @Test
    public void testOf3() throws Exception {
        EventStream<String> test = EventStream.ofWithState("test", EventCausalityEstablished.builder().build(), EventCausalityEstablished.builder().build());
        assertEquals(test.getState(), "test");
        assertEquals(test.getStream().collect(Collectors.<Event>toSet()).size(), 2);
    }

    @Test
    public void testOf4() throws Exception {
        EventStream<String> test = EventStream.ofWithState("test", Stream.of(EventCausalityEstablished.builder().build(), EventCausalityEstablished.builder().build()));
        assertEquals(test.getState(), "test");
        assertEquals(test.getStream().collect(Collectors.<Event>toSet()).size(), 2);
    }

    @Test
    public void testOf5() throws Exception {
        EventStream<String> test = EventStream.of(Stream.of(EventCausalityEstablished.builder().build(), EventCausalityEstablished.builder().build()));
        assertNull(test.getState());
        assertEquals(test.getStream().collect(Collectors.<Event>toSet()).size(), 2);
    }

}