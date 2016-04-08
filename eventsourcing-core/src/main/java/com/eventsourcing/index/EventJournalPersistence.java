/**
 * Copyright 2016 Eventsourcing team
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
package com.eventsourcing.index;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Event;
import com.eventsourcing.Journal;

import java.util.Iterator;
import java.util.Set;

public class EventJournalPersistence<T extends Event> extends JournalPersistence<T> {

    public EventJournalPersistence(Journal journal, Class<T> klass) {
        super(journal, klass);
    }

    @Override
    public Set<EntityHandle<T>> create() {
        return new EventJournalSet<>(journal, klass);
    }

    static class EventJournalSet<T extends Event> extends JournalSet<T> {

        public EventJournalSet(Journal journal, Class<T> klass) {
            super(journal, klass);
        }

        @Override
        public Iterator<EntityHandle<T>> iterator() {
            return journal.eventIterator(klass);
        }
    }
}
