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
import com.eventsourcing.repository.Journal;
import org.h2.mvstore.MVStore;

public class MVStoreMemoryRepositoryBenchmark extends RepositoryBenchmark {

    protected IndexEngine createIndex() {
        return new MVStoreIndexEngine(MVStore.open(null));
    }

    protected Journal createJournal() {
        return new MVStoreJournal(MVStore.open(null));
    }

}
