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
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex;
import com.googlecode.cqengine.index.radixreversed.ReversedRadixTreeIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import org.eventchain.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.eventchain.index.IndexEngine.IndexFeature.*;

@Component
public class MemoryIndexEngine extends AbstractIndexEngine implements IndexEngine {

    private Repository repository;

    @Reference
    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
    }

    private Journal journal;

    @Reference
    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.journal = journal;
    }

    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Arrays.asList(
                new IndexCapabilities("Hash", new IndexFeature[]{EQ, IN, QZ}, HashIndex::onAttribute),
                new IndexCapabilities("Unique", new IndexFeature[]{UNIQUE, EQ, IN}, HashIndex::onAttribute),
                new IndexCapabilities("Compound", new IndexFeature[]{COMPOUND, EQ, IN, QZ}, HashIndex::onAttribute),
                new IndexCapabilities("Navigable", new IndexFeature[]{EQ, IN, QZ, LT, GT, BT}, NavigableIndex::onAttribute),
                new IndexCapabilities("RadixTree", new IndexFeature[]{EQ, IN, SW}, RadixTreeIndex::onAttribute),
                new IndexCapabilities("ReversedRadixTree", new IndexFeature[]{EQ, IN, EW}, ReversedRadixTreeIndex::onAttribute),
                new IndexCapabilities("InvertedRadixTree", new IndexFeature[]{EQ, IN, CI}, InvertedRadixTreeIndex::onAttribute),
                new IndexCapabilities("SuffixTree", new IndexFeature[]{EQ, IN, EW, SC}, SuffixTreeIndex::onAttribute)
        );

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
