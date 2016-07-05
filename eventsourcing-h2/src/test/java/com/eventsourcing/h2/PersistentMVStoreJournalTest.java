/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import com.eventsourcing.repository.PersistentJournalTest;
import lombok.SneakyThrows;
import org.h2.mvstore.MVStore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertTrue;

@Test
public class PersistentMVStoreJournalTest extends PersistentJournalTest<MVStoreJournal> {

    public static final String FILENAME = "PersistentMVStoreJournalTest";

    static {
        new File(FILENAME).delete();
    }

    private File getFile() {
        return new File(FILENAME).getAbsoluteFile();
    }

    public PersistentMVStoreJournalTest() {
        super(createJournal());
    }

    protected static MVStoreJournal createJournal() {return new MVStoreJournal(MVStore.open("nio:" + FILENAME));}

    @Override
    @SneakyThrows
    public void reopen() {
        journal.stopAsync().awaitTerminated();
        journal = createJournal();
        journal.setRepository(repository);
        journal.startAsync().awaitRunning();
        journal.onCommandsAdded(repository.getCommands());
        journal.onEventsAdded(repository.getEvents());
    }

    @Override
    public void reopenAnother() {
        journal.stopAsync().awaitTerminated();
        journal = new MVStoreJournal(MVStore.open(null));
        journal.setRepository(repository);
        journal.startAsync().awaitRunning();
        journal.onCommandsAdded(repository.getCommands());
        journal.onEventsAdded(repository.getEvents());
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
