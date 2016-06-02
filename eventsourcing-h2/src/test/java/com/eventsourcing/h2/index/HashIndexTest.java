/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.h2.mvstore.MVStore;

public class HashIndexTest extends com.eventsourcing.index.HashIndexTest<HashIndex> {

    @Override
    public <A, O> HashIndex onAttribute(Attribute<O, A> attribute) {
        return HashIndex.onAttribute(MVStore.open(null), attribute);
    }
}