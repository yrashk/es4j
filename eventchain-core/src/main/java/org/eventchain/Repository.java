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
package org.eventchain;

import com.google.common.util.concurrent.Service;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import org.eventchain.hlc.PhysicalTimeProvider;
import org.eventchain.index.IndexEngine;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;

/**
 * Repository is holder of resources necessary to
 * facilitate Eventchain operations.
 */
public interface Repository extends Service {

    /**
     * Creates a default Repository
     * @return
     */
    static Repository create() {
        return new RepositoryImpl();
    }

    /**
     * Sets journal to be used in this repository
     *
     * Should be done before invoking {@link #startAsync()}
     *
     * @param journal
     * @throws IllegalStateException if called after the service is started
     */
    void setJournal(Journal journal) throws IllegalStateException;

    /**
     * Sets index engine to be used in this repository
     *
     * Should be done before invoking {@link #startAsync()}
     *
     * @param indexEngine
     * @throws IllegalStateException if called after the service is started
     */
    void setIndexEngine(IndexEngine indexEngine) throws IllegalStateException;

    /**
     * Get index engine as previously configured. Useful for querying.
     *
     * @return null if index engine was not configured yet.
     */
    IndexEngine getIndexEngine();

    /**
     * Sets physical time provider
     *
     * @param timeProvider
     */
    void setPhysicalTimeProvider(PhysicalTimeProvider timeProvider) throws IllegalStateException;

    /**
     * Sets lock provider
     *
     * @param lockProvider
     */
    void setLockProvider(LockProvider lockProvider) throws IllegalStateException;

    /**
     * Sets package hierarchy for command and event discovery.
     *
     * Should be done before invoking {@link #startAsync()}
     *
     * @param pkg
     * @throws IllegalStateException if called after the service is started
     */
    void setPackage(Package pkg) throws IllegalStateException;


    /**
     * Returns a set of commands discovered or configured
     * with this repository
     *
     * @return
     */
    Set<Class<? extends Command>> getCommands();

    /**
     * Returns a set of events discovered or configured
     * with this repository
     *
     * @return
     */
    Set<Class<? extends Event>> getEvents();

    /**
     * Publishes command asynchronously
     *
     * @param command
     * @param <T> Command class
     * @param <C> Result class
     * @return {@link CompletableFuture} with command's result
     */
    <T extends Command<C>, C>  CompletableFuture<C> publish(T command);

    /**
     * Shortcut method for accessing index retrieval (see {@link #query(Class, Query, QueryOptions)} with
     * {@link QueryFactory#noQueryOptions()} specified as {@link QueryOptions}
     *
     * @param klass
     * @param query
     * @param <E>
     * @return
     */
    default <E extends Entity> ResultSet<EntityHandle<E>> query(Class<E> klass, Query<EntityHandle<E>> query) {
        return query(klass, query, noQueryOptions());
    }

    /**
     * Shortcut method for accessing index querying.
     *
     * <p>Example:</p>
     *
     * {@code
     * repository.query(UserCreated.class, equal(UserCreated.EMAIL, email), noQueryOptions())
     * }
     *
     * @param klass
     * @param query
     * @param queryOptions
     * @param <E>
     * @return
     */
    default <E extends Entity> ResultSet<EntityHandle<E>> query(Class<E> klass, Query<EntityHandle<E>> query, QueryOptions queryOptions) {
        return getIndexEngine().getIndexedCollection(klass).retrieve(query, queryOptions);
    }
}
