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
