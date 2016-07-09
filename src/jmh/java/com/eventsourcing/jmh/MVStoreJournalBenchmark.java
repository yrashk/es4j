/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.Journal;
import com.eventsourcing.h2.MVStoreJournal;
import org.h2.mvstore.MVStore;

import java.io.File;

public class MVStoreJournalBenchmark extends JournalBenchmark {

    @Override protected Journal createJournal() {
        new File("benchmark_journal.db").delete();
        return new MVStoreJournal(MVStore.open("nio:benchmark_journal.db"));
    }
}
