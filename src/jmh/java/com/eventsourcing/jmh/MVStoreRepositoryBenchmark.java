/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.h2.MVStoreIndexEngine;
import com.eventsourcing.h2.MVStoreJournal;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.Journal;
import org.h2.mvstore.MVStore;

import java.io.File;

public class MVStoreRepositoryBenchmark extends RepositoryBenchmark {

    protected IndexEngine createIndex() {
        new File("benchmark_repo_index.db").delete();
        return new MVStoreIndexEngine(MVStore.open("nio:benchmark_repo_index.db"));
    }

    protected Journal createJournal() {
        new File("benchmark_repo_journal.db").delete();
        return new MVStoreJournal(MVStore.open("nio:benchmark_repo_journal.db"));
    }

}
