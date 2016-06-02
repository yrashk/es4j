/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import com.eventsourcing.Event;
import com.eventsourcing.PersistentJournalTest;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.h2.mvstore.MVStore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

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

    public static class TestEvent extends Event {
        @Getter @Setter
        private String value;
    }

    @Test
    @SneakyThrows
    public void unrecognizedEntities() {
        journal.onEventsAdded(Collections.singletonList(TestEvent.class).stream().collect(Collectors.toSet()));
        journal.getStore().commit();
        journal.getStore().close();
        journal = new MVStoreJournal(MVStore.open("nio:" + FILENAME));
        journal.initializeStore();
        List<MVStoreJournal.LayoutInformation> unrecognizedEntities = journal.getUnrecognizedEntities();
        assertFalse(unrecognizedEntities.isEmpty());
        assertEquals(unrecognizedEntities.size(), 1);
        MVStoreJournal.LayoutInformation entity = unrecognizedEntities.get(0);
        assertEquals(entity.className(), TestEvent.class.getName());
        assertEquals(entity.properties().size(), 2);
        assertEquals(entity.properties().get(1).name(), "value");
        assertEquals(entity.properties().get(1).type(), "java.lang.String");
    }
}
