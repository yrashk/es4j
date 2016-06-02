/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class EntityTest {

    @Test
    public void uuidGeneration() {
        Entity entity = new Entity();
        UUID uuid = entity.uuid();
        assertNotNull(uuid);
        assertEquals(uuid, entity.uuid());
    }

}