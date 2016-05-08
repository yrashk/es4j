/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.quantizer.Quantizer;

public class MemoryNavigableIndexTest extends NavigableIndexTest<NavigableIndex> {

    @Override
    public <A extends Comparable<A>, O> NavigableIndex onAttribute(Attribute<O, A> attribute) {
        return NavigableIndex.onAttribute(attribute);
    }

    @Override
    public <A extends Comparable<A>, O> Index<O> withQuantizerOnAttribute(Quantizer<A> quantizer, Attribute<O, A> attribute) {
        return NavigableIndex.withQuantizerOnAttribute(quantizer, attribute);
    }


}