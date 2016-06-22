/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Repository;
import com.eventsourcing.StandardEntity;
import com.eventsourcing.repository.Journal;
import com.eventsourcing.inmem.MemoryJournal;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;

public class CascadingIndexEngineTest {

    private static class FailingIndexEngine extends CQIndexEngine {

        @Override
        protected List<IndexCapabilities> getIndexMatrix() {
            return Arrays.asList();
        }

        @Override
        public void setJournal(Journal journal) throws IllegalStateException {
            this.journal = journal;
        }

        @Override
        public void setRepository(Repository repository) throws IllegalStateException {
            this.repository = repository;
        }
    }

    private static class MyEntity extends StandardEntity {}

    private static SimpleAttribute<MyEntity, UUID> INDEX = new SimpleAttribute<MyEntity, UUID>("idx") {
        @Override
        public UUID getValue(MyEntity object, QueryOptions queryOptions) {
            return UUID.randomUUID();
        }
    };

    @Test @SneakyThrows
    public void cascading() {
        MemoryJournal journal = new MemoryJournal();
        CascadingIndexEngine indexEngine = new CascadingIndexEngine(new FailingIndexEngine(), new MemoryIndexEngine());
        indexEngine.setJournal(journal);

        assertNotNull(indexEngine.getIndexOnAttribute(INDEX, IndexEngine.IndexFeature.LT));
    }

    @Test(expectedExceptions = IndexEngine.IndexNotSupported.class) @SneakyThrows
    public void exhaustingOptions() {
        MemoryJournal journal = new MemoryJournal();
        CascadingIndexEngine indexEngine = new CascadingIndexEngine(new FailingIndexEngine());
        indexEngine.setJournal(journal);

        indexEngine.getIndexOnAttribute(INDEX, IndexEngine.IndexFeature.LT);
    }

}