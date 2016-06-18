/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.repository.Journal;
import com.eventsourcing.repository.MemoryJournal;
import com.eventsourcing.repository.RepositoryImpl;
import org.testng.annotations.Test;

@Test
public class MemoryJournalRepositoryTest extends RepositoryTest<RepositoryImpl> {
    public MemoryJournalRepositoryTest() {
        super(new RepositoryImpl());
    }

    protected Journal createJournal() {
        return new MemoryJournal();
    }
}
