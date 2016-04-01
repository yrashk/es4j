/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
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
