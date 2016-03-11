package org.eventchain.h2;

import org.eventchain.JournalTest;
import org.h2.mvstore.MVStore;

import static org.testng.Assert.*;

public class MVStoreJournalTest extends JournalTest<MVStoreJournal> {

    public MVStoreJournalTest() {
        super(new MVStoreJournal(MVStore.open(null)));
    }
}