/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import com.eventsourcing.Journal;
import com.eventsourcing.repository.RepositoryTest;
import com.eventsourcing.repository.StandardRepository;
import org.h2.mvstore.MVStore;
import org.testng.annotations.Test;

@Test
public class MVStoreJournalRepositoryTest extends RepositoryTest<StandardRepository> {
    public MVStoreJournalRepositoryTest() {
        super(new StandardRepository());
    }

    @Override
    protected Journal createJournal() {
        return new MVStoreJournal(MVStore.open(null));
    }
}
