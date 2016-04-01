package org.eventchain.jmh;

import org.eventchain.Journal;
import org.eventchain.h2.MVStoreIndexEngine;
import org.eventchain.h2.MVStoreJournal;
import org.eventchain.index.IndexEngine;
import org.h2.mvstore.MVStore;

public class MVStoreMemoryRepositoryBenchmark extends RepositoryBenchmark {

    protected IndexEngine createIndex() {
        return new MVStoreIndexEngine(MVStore.open(null));
    }

    protected Journal createJournal() {
        return new MVStoreJournal(MVStore.open(null));
    }

}
