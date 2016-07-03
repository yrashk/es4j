/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.inmem;

import com.eventsourcing.Journal;
import com.eventsourcing.repository.RepositoryTest;
import com.eventsourcing.repository.StandardRepository;
import org.testng.annotations.Test;

@Test
public class MemoryJournalRepositoryTest extends RepositoryTest<StandardRepository> {
    public MemoryJournalRepositoryTest() {
        super(new StandardRepository());
    }

    protected Journal createJournal() {
        return new MemoryJournal();
    }
}
