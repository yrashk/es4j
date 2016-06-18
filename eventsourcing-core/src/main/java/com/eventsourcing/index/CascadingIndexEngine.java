/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.repository.Journal;
import com.eventsourcing.Repository;
import com.google.common.base.Joiner;
import com.googlecode.cqengine.index.Index;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cascading index engine allows to combine multiple engines behind a singular {@link IndexEngine} interface.
 * <p>
 * The order of index engines is important is evaluated "left to right", i.e. those added earlier will be given a higher
 * priority. The way cascading works is if an index engine in the list does not support a specific type of index,
 * next one in the list will be tried, until the list is exhausted.
 * <p>
 * In an OSGi environment, <code>indexEngine</code> property must be configured to ensure intended ordering. This property
 * should have a comma-delimited list of fully qualified class names for index engines to be used. It can list index engines
 * that are not available through references. They will be ignored.
 */
@Slf4j
@Component(property = {"indexEngines=", "type=CascadingIndexEngine"}, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CascadingIndexEngine extends CQIndexEngine implements IndexEngine {

    private List<IndexEngine> indexEngines = new ArrayList<>();
    private String[] indexEngineNames;

    public CascadingIndexEngine() {
    }

    public CascadingIndexEngine(IndexEngine... indexEngines) {
        this.indexEngines = Arrays.asList(indexEngines);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
               target = "(!(service.pid=com.eventsourcing.index.CascadingIndexEngine))")
    public void addIndexEngine(IndexEngine indexEngine) {
        indexEngines.add(indexEngine);
        sortIndexEngines();
    }

    protected void sortIndexEngines() {
        if (indexEngineNames != null) {
            indexEngines =
                    Arrays.asList(indexEngineNames).stream().
                            map(name -> indexEngines.stream().filter(e -> e.getClass().getName().contentEquals(name))
                                                    .findFirst()).
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
    public <O extends Entity, A> Index<O> getIndexOnAttribute(Attribute<O, A> attribute, IndexFeature... features)
            throws IndexNotSupported {
        for (IndexEngine engine : indexEngines) {
            try {
                return engine.getIndexOnAttribute(attribute, features);
            } catch (IndexNotSupported e) {
            }
        }
        throw new IndexNotSupported(new Attribute[]{attribute}, features, this);
    }

    @Override
    public <O extends Entity, A> Index<O> getIndexOnAttributes(Attribute<O, A>[] attributes, IndexFeature... features)
            throws IndexNotSupported {
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
