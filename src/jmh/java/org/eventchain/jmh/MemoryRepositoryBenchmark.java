package org.eventchain.jmh;

import org.eventchain.Journal;
import org.eventchain.MemoryJournal;
import org.eventchain.index.IndexEngine;
import org.eventchain.index.MemoryIndexEngine;

public class MemoryRepositoryBenchmark extends RepositoryBenchmark {
    protected IndexEngine createIndex() {
        return new MemoryIndexEngine();
    }

    protected Journal createJournal() {
        return new MemoryJournal();
    }

}
