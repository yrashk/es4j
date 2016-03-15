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
package org.eventchain.index;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import org.eventchain.*;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CQIndexEngine extends AbstractIndexEngine {
    protected Repository repository;

    @Reference
    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
    }

    protected Journal journal;

    @Reference
    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.journal = journal;
    }

    protected Map<String, IndexedCollection> indexedCollections = new ConcurrentHashMap<>();

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> IndexedCollection<EntityHandle<T>> getIndexedCollection(Class<T> klass) {
        IndexedCollection existingCollection = indexedCollections.get(klass.getName());
        if (existingCollection == null) {

            JournalPersistence<T> tJournalPersistence = null;

            if (Event.class.isAssignableFrom(klass))
                tJournalPersistence = (JournalPersistence<T>) new EventJournalPersistence<>(journal, (Class<Event>) klass);
            if (Command.class.isAssignableFrom(klass))
                tJournalPersistence = (JournalPersistence<T>) new CommandJournalPersistence<>(journal, (Class<Command>) klass);

            if (tJournalPersistence == null) {
                throw new IllegalArgumentException();
            }

            ConcurrentIndexedCollection<EntityHandle<T>> indexedCollection = new ConcurrentIndexedCollection<>(tJournalPersistence);
            indexedCollections.put(klass.getName(), indexedCollection);
            return indexedCollection;
        } else {
            return existingCollection;
        }
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }

}
