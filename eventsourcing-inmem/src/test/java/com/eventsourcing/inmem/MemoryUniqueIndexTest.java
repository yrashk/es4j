/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.inmem;

import com.eventsourcing.Entity;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.UniqueIndexTest;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import org.testng.annotations.Test;

@Test
public class MemoryUniqueIndexTest extends UniqueIndexTest<UniqueIndex> {

    @Override
    public <A, O extends Entity> UniqueIndex onAttribute(Attribute<O, A> attribute) {
        return UniqueIndex.onAttribute(attribute);
    }
}
