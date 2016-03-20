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
package org.eventchain.h2;

import com.googlecode.cqengine.attribute.Attribute;
import org.eventchain.Journal;
import org.eventchain.Repository;
import org.eventchain.h2.index.HashIndex;
import org.eventchain.h2.index.UniqueIndex;
import org.eventchain.index.CQIndexEngine;
import org.eventchain.index.IndexEngine;
import org.h2.mvstore.MVStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

import static org.eventchain.index.IndexEngine.IndexFeature.*;

@Component(properties = "index.properties")
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
        this.store = MVStore.open((String) ctx.getProperties().get("filename"));
    }

    @Override
    protected void doStop() {
        this.store.close();
        super.doStop();
    }

    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Arrays.asList(
            new IndexCapabilities<Attribute>("Hash", new IndexFeature[]{EQ, IN, QZ}, attribute -> HashIndex.onAttribute(store, attribute)),
            new IndexCapabilities<Attribute>("Unique", new IndexFeature[]{UNIQUE, EQ, IN}, attribute -> UniqueIndex.onAttribute(store, attribute))
        );
    }
}
