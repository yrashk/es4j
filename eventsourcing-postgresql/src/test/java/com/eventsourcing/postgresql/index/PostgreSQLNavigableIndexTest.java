/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.NavigableIndexTest;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.quantizer.Quantizer;
import org.testng.annotations.Test;

import static com.eventsourcing.postgresql.PostgreSQLTest.dataSource;

@Test
public class PostgreSQLNavigableIndexTest extends NavigableIndexTest<NavigableIndex> {


    @Override
    public <A extends Comparable<A>, O extends Entity> NavigableIndex onAttribute(Attribute<O, A> attribute) {
        return NavigableIndex.onAttribute(dataSource, attribute);
    }

    @Override
    public <A extends Comparable<A>, O extends Entity> Index<EntityHandle<O>>
    withQuantizerOnAttribute(Quantizer<A> quantizer, com.eventsourcing.index.Attribute<O, A> attribute) {
        return NavigableIndex.withQuantizerOnAttribute(dataSource, quantizer, attribute);
    }

}
