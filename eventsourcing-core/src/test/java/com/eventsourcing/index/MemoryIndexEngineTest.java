/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

public class MemoryIndexEngineTest extends IndexEngineTest<MemoryIndexEngine> {
    public MemoryIndexEngineTest() {
        super(new MemoryIndexEngine());
    }
}
