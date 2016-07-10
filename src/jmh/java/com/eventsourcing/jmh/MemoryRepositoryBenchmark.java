/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.Journal;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.inmem.MemoryJournal;

public class MemoryRepositoryBenchmark extends RepositoryBenchmark {
    protected IndexEngine createIndex() {
        return new MemoryIndexEngine();
    }

    protected Journal createJournal() {
        return new MemoryJournal();
    }

}
