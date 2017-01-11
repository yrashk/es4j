/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.EntitySubscriber;
import com.eventsourcing.Repository;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.IndexLoader;
import com.eventsourcing.inmem.MemoryIndexEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.SneakyThrows;
import lombok.Value;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class QuerySubscriber implements EntitySubscriber<Entity> {

    private Repository repository;

    private final Set<IndexLoader> indexLoaders = new LinkedHashSet<>();

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE)
    public void bindIndexLoader(IndexLoader loader) {
        indexLoaders.add(loader);
    }

    public void unbindIndexLoader(IndexLoader loader) {
        indexLoaders.remove(loader);
    }

    @Reference
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void unsetRepository(Repository repository) {
        this.repository = null;
    }

    public QuerySubscriber() {
    }

    public QuerySubscriber(Repository repository) {
        this.repository = repository;
    }

    @Value
    private static class QueryHolder<E extends Entity> {
        private Class<E> klass;
        private Query<EntityHandle<E>> query;

        public int hashCode() {
            return query.hashCode();
        }
    }
    private Map<QueryHolder, Runnable> queries = new HashMap<>();

    public <E extends Entity> void addQuery(Class<E> klass, Query<EntityHandle<E>> query,
                                            Consumer<Query<EntityHandle<E>>> consumer) {
        queries.put(new QueryHolder<>(klass, query), () -> consumer.accept(query));
    }

    public <E extends Entity> void removeQuery(Class<E> klass, Query<EntityHandle<E>> query) {
        queries.remove(new QueryHolder<>(klass, query));
    }


    @SneakyThrows
    @Override public void accept(Repository repository, Stream<EntityHandle<Entity>> entityStream) {
        MemoryIndexEngine indexEngine = new MemoryIndexEngine();
        indexEngine.setJournal(repository.getJournal());
        indexEngine.setRepository(repository);

        for (Class<? extends Entity> klass : repository.getCommands()) {
            configureIndices(indexEngine, klass);
        }
        for (Class<? extends Entity> klass : repository.getEvents()) {
            configureIndices(indexEngine, klass);
        }

        indexEngine.startAsync().awaitRunning();

        entityStream.forEachOrdered(h -> {
            Entity entity = h.get();
            IndexedCollection<EntityHandle<Entity>> indexedCollection =
                    indexEngine.getIndexedCollection((Class<Entity>)entity.getClass());
            indexedCollection.add(h);
        });

        //
        queries.forEach((query, runnable) -> {
            IndexedCollection collection = indexEngine.getIndexedCollection(query.getKlass());
            try (ResultSet resultSet = collection.retrieve(query.getQuery())) {
                if (resultSet.isNotEmpty()) {
                    runnable.run();
                }
            }
        });

        //
        indexEngine.stopAsync().awaitTerminated();
    }

    private void configureIndices(IndexEngine engine, Class<? extends Entity> klass) throws IndexEngine.IndexNotSupported {
        for (IndexLoader indexLoader : indexLoaders) {
            Iterable<Index> indices = indexLoader.load(engine, klass);
            for (Index i : indices) {
                IndexedCollection<? extends EntityHandle<? extends Entity>> collection =
                        engine.getIndexedCollection(klass);
                boolean hasIndex = StreamSupport.stream(collection.getIndexes().spliterator(), false)
                                                .anyMatch(index -> index.equals(i));
                if (!hasIndex) {
                    collection.addIndex(i);
                }
            }
        }
    }
}
