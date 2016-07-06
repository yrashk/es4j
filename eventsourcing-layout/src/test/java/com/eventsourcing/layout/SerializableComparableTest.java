/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SerializableComparableTest {
    static class TestClass implements SerializableComparable<String> {
        @Override public String getSerializableComparable() {
            return "test";
        }
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(SerializableComparable.getType(TestClass.class), String.class);
    }
}