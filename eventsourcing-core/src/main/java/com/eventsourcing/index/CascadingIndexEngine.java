/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.Repository;
import com.eventsourcing.Journal;
import com.google.common.base.Joiner;
import com.googlecode.cqengine.index.Index;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import javax.management.openmbean.*;
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
@Component(property = {"indexEngines=", "type=CascadingIndexEngine",
                      "jmx.objectname=com.eventsourcing.index.IndexEngine:type=CascadingIndexEngine"
                      }, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CascadingIndexEngine extends CQIndexEngine implements IndexEngine, CascadingIndexEngineMBean {

    @Override public String getType() {
        return "CascadingIndexEngine";
    }

    private List<IndexEngine> indexEngines = new ArrayList<>();
    @Getter
    private String[] configuredIndexEngines;

    public CascadingIndexEngine() {
    }

    public CascadingIndexEngine(IndexEngine... indexEngines) {
        this.indexEngines = Arrays.asList(indexEngines);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               target = "(!(service.pid=com.eventsourcing.index.CascadingIndexEngine))")
    public void addIndexEngine(IndexEngine indexEngine) {
        indexEngine.setJournal(journal);
        indexEngine.setRepository(repository);

        indexEngines.add(indexEngine);
        sortIndexEngines();
    }

    protected void sortIndexEngines() {
        if (configuredIndexEngines != null) {
            indexEngines =
                    Arrays.asList(configuredIndexEngines).stream().
                            map(name -> indexEngines.stream().filter(e -> e.getType().contentEquals(name))
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
        configuredIndexEngines = ((String) ctx.getProperties().get("indexEngines")).split(",");
        sortIndexEngines();
    }

    public String[] getIndexEngines() {
        return indexEngines.stream().map(IndexEngine::getType).toArray(String[]::new);
    }

    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        this.journal = journal;
    }

    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        this.repository = repository;
    }

    private Map<String, IndexEngine> decisions = new HashMap<>();

    @Override
    public <O extends Entity, A> Index<O> getIndexOnAttribute(Attribute<O, A> attribute, IndexFeature... features)
            throws IndexNotSupported {
        for (IndexEngine engine : indexEngines) {
            try {
                Index<O> index = engine.getIndexOnAttribute(attribute, features);
                decisions.put(Joiner.on(", ").join(features) + " on " + attribute.toString(), engine);
                return index;
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
                Index<O> index = engine.getIndexOnAttributes(attributes, features);
                for (Attribute attribute : attributes) {
                    decisions.put(Joiner.on(", ").join(features) + " on " + attribute.toString(), engine);
                }
                return index;
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
        super.doStart();
    }

    // MBean
    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Collections.emptyList();
    }

    @SneakyThrows
    @Override public TabularData getCascadingDecisions() {
        CompositeType type = new CompositeType("Decision", "Cascading decision",
                                               new String[]{"Index", "Engine"},
                                               new String[]{"Index", "Index Engine"},
                                               new OpenType[]{SimpleType.STRING, SimpleType.STRING});
        TabularDataSupport tab = new TabularDataSupport(
                new TabularType("Decisions", "Cascading decisions", type, new String[]{"Index"}));
        for (Map.Entry<String, IndexEngine> entry : decisions.entrySet()) {
            tab.put(new CompositeDataSupport(type,
                                             new String[]{"Index", "Engine"},
                                             new Object[]{entry.getKey(), entry.getValue().getType()}));
        }

        return tab;
    }
}
