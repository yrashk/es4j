/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.*;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CQIndexEngine extends AbstractIndexEngine {
    protected Repository repository;
    protected Journal journal;


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
