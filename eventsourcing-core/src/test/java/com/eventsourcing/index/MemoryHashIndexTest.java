/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;

public class MemoryHashIndexTest extends HashIndexTest<HashIndex> {
    @Override
    public <A, O> HashIndex onAttribute(Attribute<O, A> attribute) {
        return HashIndex.onAttribute(attribute);
    }
}
