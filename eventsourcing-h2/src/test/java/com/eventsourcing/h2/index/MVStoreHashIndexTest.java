/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2.index;

import com.eventsourcing.Entity;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.EqualityIndexTest;
import org.h2.mvstore.MVStore;
import org.testng.annotations.Test;

@Test
public class MVStoreHashIndexTest extends EqualityIndexTest<HashIndex> {

    @Override
    public <A, O extends Entity> HashIndex onAttribute(Attribute<O, A> attribute) {
        return HashIndex.onAttribute(MVStore.open(null), attribute);
    }
}