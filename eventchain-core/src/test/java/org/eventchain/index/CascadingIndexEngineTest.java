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

import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.SneakyThrows;
import org.eventchain.Entity;
import org.eventchain.Journal;
import org.eventchain.MemoryJournal;
import org.eventchain.Repository;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.eventchain.index.IndexEngine.IndexFeature.LT;
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

    private static class MyEntity extends Entity {}
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

        assertNotNull(indexEngine.getIndexOnAttribute(INDEX, LT));
    }

    @Test(expectedExceptions = IndexEngine.IndexNotSupported.class) @SneakyThrows
    public void exhaustingOptions() {
        MemoryJournal journal = new MemoryJournal();
        CascadingIndexEngine indexEngine = new CascadingIndexEngine(new FailingIndexEngine());
        indexEngine.setJournal(journal);

        indexEngine.getIndexOnAttribute(INDEX, LT);
    }

}