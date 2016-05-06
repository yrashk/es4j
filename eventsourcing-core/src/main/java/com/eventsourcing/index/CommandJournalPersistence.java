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

import com.eventsourcing.Command;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.Journal;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Iterator;
import java.util.Set;

public class CommandJournalPersistence<T extends Command<?>> extends JournalPersistence<T> {
    public CommandJournalPersistence(Journal journal, Class<T> klass) {
        super(journal, klass);
    }

    @Override
    public ObjectStore<EntityHandle<T>> createObjectStore() {
        return new CommandJournalObjectStore<>(journal, klass);
    }

    @Override
    public boolean supportsIndex(Index<EntityHandle<T>> index) {
        return true;
    }

    @Override
    public void openRequestScopeResources(QueryOptions queryOptions) {

    }

    @Override
    public void closeRequestScopeResources(QueryOptions queryOptions) {

    }

    static class CommandJournalObjectStore<T extends Command<?>> extends JournalObjectStore<T> {

        public CommandJournalObjectStore(Journal journal, Class<T> klass) {
            super(journal, klass);
        }

        @Override
        public CloseableIterator<EntityHandle<T>> iterator(QueryOptions queryOptions) {
            return journal.commandIterator(klass);
        }
    }
}
