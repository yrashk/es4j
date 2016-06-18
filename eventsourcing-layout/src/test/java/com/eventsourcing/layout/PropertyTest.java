/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import lombok.SneakyThrows;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PropertyTest {

    @Test
    @SneakyThrows
    public void propertyLayout() {
        Layout<Property> layout = new Layout<>(Property.class);
        assertEquals(layout.getName(), "rfc.eventsourcing.com/spec:7/LDL/#Property");
        assertEquals(layout.getProperties().size(), 2);
        assertTrue(layout.getProperties().stream().anyMatch(p -> p.getName().contentEquals("name")));
        assertTrue(layout.getProperties().stream().anyMatch(p -> p.getName().contentEquals("fingerprint")));
    }
}