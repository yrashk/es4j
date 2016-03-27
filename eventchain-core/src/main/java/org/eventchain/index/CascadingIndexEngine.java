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

import com.google.common.base.Joiner;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import lombok.extern.slf4j.Slf4j;
import org.eventchain.Entity;
import org.eventchain.EntityHandle;
import org.eventchain.Journal;
import org.eventchain.Repository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cascading index engine allows to combine multiple engines behind a singular {@link IndexEngine} interface.
 *
 * The order of index engines is important is evaluated "left to right", i.e. those added earlier will be given a higher
 * priority. The way cascading works is if an index engine in the list does not support a specific type of index,
 * next one in the list will be tried, until the list is exhausted.
 *
 * In an OSGi environment, <code>indexEngine</code> property must be configured to ensure intended ordering. This property
 * should have a comma-delimited list of fully qualified class names for index engines to be used. It can list index engines
 * that are not available through references. They will be ignored.
 */
@Slf4j
@Component(property = {"indexEngines=", "type=org.eventchain.index.CascadingIndexEngine"}, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CascadingIndexEngine  extends CQIndexEngine implements IndexEngine {

    private List<IndexEngine> indexEngines = new ArrayList<>();
    private String[] indexEngineNames;

    public CascadingIndexEngine() {
    }

    public CascadingIndexEngine(IndexEngine ...indexEngines) {
        this.indexEngines = Arrays.asList(indexEngines);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, target = "(!(service.pid=org.eventchain.index.CascadingIndexEngine))")
    public void addIndexEngine(IndexEngine indexEngine) {
        indexEngines.add(indexEngine);
        sortIndexEngines();
    }

    protected void sortIndexEngines() {
        if (indexEngineNames != null) {
            indexEngines =
                    Arrays.asList(indexEngineNames).stream().
                            map(name -> indexEngines.stream().filter(e -> e.getClass().getName().contentEquals(name)).findFirst()).
                            filter(Optional::isPresent).
                            map(Optional::get).
                            collect(Collectors.toList());
        }
    }

    public void removeIndexEngine(IndexEngine indexEngine) {
        indexEngines.remove(indexEngine);
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        indexEngineNames = ((String) ctx.getProperties().get("indexEngines")).split(",");
        sortIndexEngines();
    }

    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Collections.emptyList();
    }

    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        this.journal = journal;
    }

    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        this.repository = repository;
    }

    @Override
    public <A, O> Index<A> getIndexOnAttribute(Attribute<A, O> attribute, IndexFeature... features) throws IndexNotSupported {
        for (IndexEngine engine : indexEngines) {
            try {
                return engine.getIndexOnAttribute(attribute, features);
            } catch (IndexNotSupported e) {
            }
        }
        throw new IndexNotSupported(new Attribute[]{attribute}, features, this);
    }

    @Override
    public <A, O> Index<A> getIndexOnAttributes(Attribute<A, O>[] attributes, IndexFeature... features) throws IndexNotSupported {
        for (IndexEngine engine : indexEngines) {
            try {
                return engine.getIndexOnAttributes(attributes, features);
            } catch (IndexNotSupported e) {
            }
        }
        throw new IndexNotSupported(attributes, features, this);
    }

    @Override
    public String toString() {
        return "CascadingIndexEngine[" + Joiner.on(", ").join(indexEngines) + "]";
    }

    @Override
    protected void doStart() {
        indexEngines.forEach(engine -> engine.setJournal(journal));
        indexEngines.forEach(engine -> engine.setRepository(repository));
        super.doStart();
    }
}
