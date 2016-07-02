/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import com.eventsourcing.repository.JournalTest;
import lombok.extern.slf4j.Slf4j;
import org.h2.mvstore.MVStore;
import org.testng.annotations.Test;

@Slf4j
@Test
public class MVStoreJournalTest extends JournalTest<MVStoreJournal> {

    private final MVStore store;

    public MVStoreJournalTest() {
        super(new MVStoreJournal(MVStore.open(null)));
        store = journal.getStore();
    }

}