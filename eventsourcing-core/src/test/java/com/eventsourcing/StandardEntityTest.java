/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.layout.Layout;
import lombok.SneakyThrows;
import lombok.Value;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class StandardEntityTest {

    @Value
    public static class SomeEntity extends StandardEntity<SomeEntity> {
        String a;
    }

    @Test
    @SneakyThrows
    public void layout() {
        Layout<SomeEntity> layout = Layout.forClass(SomeEntity.class);
        assertEquals(layout.getProperties().size(), 2);
        assertNotNull(layout.getProperty("a"));
        assertNotNull(layout.getProperty("timestamp"));
    }

}