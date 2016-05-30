package com.eventsourcing.jmh;

import com.eventsourcing.Journal;
import com.eventsourcing.h2.MVStoreJournal;
import com.eventsourcing.index.IndexEngine;
import org.h2.mvstore.MVStore;

import java.io.File;

public class MVStoreJournalBenchmark extends JournalBenchmark {

    @Override protected Journal createJournal() {
        System.out.println("createJ");
        new File("benchmark_journal.db").delete();
        return new MVStoreJournal(MVStore.open("nio:benchmark_journal.db"));
    }
}
