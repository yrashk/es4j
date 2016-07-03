/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import com.eventsourcing.repository.PersistentJournalTest;
import org.h2.mvstore.MVStore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class PersistentMVStoreJournalTest extends PersistentJournalTest<MVStoreJournal> {

    public static final String FILENAME = "PersistentMVStoreJournalTest";

    static {
        new File(FILENAME).delete();
    }

    private File getFile() {
        return new File(FILENAME).getAbsoluteFile();
    }

    public PersistentMVStoreJournalTest() {
        super(new MVStoreJournal(MVStore.open("nio:" + FILENAME)));
    }

    @Override
    public void reopen() {
        MVStore store = journal.getStore();
        store.close();
        journal.setStore(MVStore.open("nio:" + FILENAME));
        journal.initializeStore();
    }

    @Override
    public void reopenAnother() {
        MVStore store = journal.getStore();
        store.close();
        journal.setStore(MVStore.open(null));
        journal.initializeStore();
    }

    @BeforeClass
    @Override
    public void setUpEnv() throws Exception {
        super.setUpEnv();
        assertTrue(getFile().exists());
    }

    @AfterClass
    @Override
    public void tearDownEnv() throws Exception {
        super.tearDownEnv();
        assertTrue(getFile().exists());
        if (getFile().exists()) {
            assertTrue(getFile().delete());
        }
    }

}
