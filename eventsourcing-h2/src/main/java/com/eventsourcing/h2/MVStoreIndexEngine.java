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
package com.eventsourcing.h2;

import com.eventsourcing.Journal;
import com.eventsourcing.Repository;
import com.eventsourcing.h2.index.HashIndex;
import com.eventsourcing.h2.index.UniqueIndex;
import com.eventsourcing.index.CQIndexEngine;
import com.eventsourcing.index.IndexEngine;
import com.googlecode.cqengine.attribute.Attribute;
import org.h2.mvstore.MVStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Arrays;
import java.util.List;

@Component(property = {"filename=index.db", "type=MVStoreIndexEngine"})
public class MVStoreIndexEngine extends CQIndexEngine implements IndexEngine {

    private MVStore store;

    public MVStoreIndexEngine() {}

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

    public MVStoreIndexEngine(MVStore store) {
        this.store = store;
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        store = MVStore.open((String) ctx.getProperties().get("filename"));
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        store.close();
    }

    @Override
    protected void doStop() {
        this.store.close();
        super.doStop();
    }

    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Arrays.asList(
            new IndexCapabilities<Attribute>("Hash", new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.QZ}, attribute -> HashIndex.onAttribute(store, attribute)),
            new IndexCapabilities<Attribute>("Unique", new IndexFeature[]{IndexFeature.UNIQUE, IndexFeature.EQ, IndexFeature.IN}, attribute -> UniqueIndex.onAttribute(store, attribute))
        );
    }

    @Override
    public String toString() {
        return "MVStoreIndexEngine[" + store.getFileStore().getFileName() + "]";
    }
}
