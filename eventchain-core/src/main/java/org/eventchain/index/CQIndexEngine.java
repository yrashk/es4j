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
