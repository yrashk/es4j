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

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.compound.CompoundIndex;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex;
import com.googlecode.cqengine.index.radixreversed.ReversedRadixTreeIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import org.eventchain.Journal;
import org.eventchain.Repository;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

import static org.eventchain.index.IndexEngine.IndexFeature.*;

@Component(property = {"type=org.eventchain.index.MemoryIndexEngine"})
public class MemoryIndexEngine extends CQIndexEngine implements IndexEngine {

    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
    }

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
                new IndexCapabilities<Attribute>("Hash", new IndexFeature[]{EQ, IN, QZ}, HashIndex::onAttribute),
                new IndexCapabilities<Attribute>("Unique", new IndexFeature[]{UNIQUE, EQ, IN}, UniqueIndex::onAttribute),
                new IndexCapabilities<Attribute[]>("Compound", new IndexFeature[]{COMPOUND, EQ, IN, QZ}, CompoundIndex::onAttributes),
                new IndexCapabilities<Attribute>("Navigable", new IndexFeature[]{EQ, IN, QZ, LT, GT, BT}, NavigableIndex::onAttribute),
                new IndexCapabilities<Attribute>("RadixTree", new IndexFeature[]{EQ, IN, SW}, RadixTreeIndex::onAttribute),
                new IndexCapabilities<Attribute>("ReversedRadixTree", new IndexFeature[]{EQ, IN, EW}, ReversedRadixTreeIndex::onAttribute),
                new IndexCapabilities<Attribute>("InvertedRadixTree", new IndexFeature[]{EQ, IN, CI}, InvertedRadixTreeIndex::onAttribute),
                new IndexCapabilities<Attribute>("SuffixTree", new IndexFeature[]{EQ, IN, EW, SC}, SuffixTreeIndex::onAttribute)
        );

    }
}
