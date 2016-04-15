/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package com.eventsourcing.h2.index;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.quantizer.Quantizer;
import org.h2.mvstore.MVStore;

public class NavigableIndexTest extends com.eventsourcing.index.NavigableIndexTest<NavigableIndex> {

    @Override
    public <A extends Comparable<A>, O> NavigableIndex onAttribute(Attribute<O, A> attribute) {
        return NavigableIndex.onAttribute(MVStore.open(null), attribute);
    }

    @Override
    public <A extends Comparable<A>, O> Index<O> withQuantizerOnAttribute(Quantizer<A> quantizer, Attribute<O, A> attribute) {
        return null;
    }
}