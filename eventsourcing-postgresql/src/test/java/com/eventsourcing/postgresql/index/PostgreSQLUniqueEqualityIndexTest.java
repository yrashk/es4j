/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql.index;

import com.eventsourcing.Entity;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.UniqueIndexTest;
import com.eventsourcing.postgresql.PostgreSQLTest;
import org.testng.annotations.Test;

@Test
public class PostgreSQLUniqueEqualityIndexTest extends UniqueIndexTest<EqualityIndex> {

    @Override
    public <A, O extends Entity> EqualityIndex onAttribute(Attribute<O, A> attribute) {
        return EqualityIndex.onAttribute(PostgreSQLTest.dataSource, attribute, true);
    }

}
