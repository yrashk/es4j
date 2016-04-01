package org.eventchain.jmh;

import org.eventchain.Journal;
import org.eventchain.h2.MVStoreIndexEngine;
import org.eventchain.h2.MVStoreJournal;
import org.eventchain.index.IndexEngine;
import org.h2.mvstore.MVStore;

import java.io.File;

public class MVStoreRepositoryBenchmark extends RepositoryBenchmark {

    protected IndexEngine createIndex() {
        new File("benchmark_index.db").delete();
        return new MVStoreIndexEngine(MVStore.open("nio:benchmark_index.db"));
    }

    protected Journal createJournal() {
        new File("benchmark_journal.db").delete();
        return new MVStoreJournal(MVStore.open("nio:benchmark_journal.db"));
    }

}
