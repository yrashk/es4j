/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.hlc.PhysicalTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.google.common.util.concurrent.Service;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;

/**
 * Repository is holder of resources necessary to
 * facilitate Eventsourcing operations.
 */
public interface Repository extends Service {

    /**
     * Creates a default Repository.
     * <p>
     * Default setup:
     * <p>
     * <ul>
     * <li>Default {@link NTPServerTimeProvider} is set with {@link #setPhysicalTimeProvider(PhysicalTimeProvider)}</li>
     * <li>Default {@link MemoryLockProvider} is set with {@link #setLockProvider(LockProvider)}</li>
     * </ul>
     *
     * @return
     */
    static Repository create() throws Exception {
        RepositoryImpl repository = new RepositoryImpl();
        PhysicalTimeProvider timeProvider = new NTPServerTimeProvider();
        repository.setPhysicalTimeProvider(timeProvider);
        LockProvider lockProvider = new MemoryLockProvider();
        repository.setLockProvider(lockProvider);
        return repository;
    }

    /**
     * Sets journal to be used in this repository
     * <p>
     * Should be done before invoking {@link #startAsync()}
     *
     * @param journal
     * @throws IllegalStateException if called after the service is started
     */
    void setJournal(Journal journal) throws IllegalStateException;

    /**
     * Sets index engine to be used in this repository
     * <p>
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
     * Adds a command set provider. Will fetch a command set upon initialization
     *
     * @param provider
     */
    void addCommandSetProvider(CommandSetProvider provider);

    /**
     * Removes a command set provider
     *
     * @param provider
     */
    void removeCommandSetProvider(CommandSetProvider provider);

    /**
     * Adds an event set provider. Will fetch an event set upon initialization
     *
     * @param provider
     */
    void addEventSetProvider(EventSetProvider provider);

    /**
     * Removes an event set provider
     *
     * @param provider
     */
    void removeEventSetProvider(EventSetProvider provider);

    /**
     * Adds an entity subscriber
     * @param subscriber
     */
    void addEntitySubscriber(EntitySubscriber subscriber);

    /**
     * Removes an entity subscriber
     * @param subscriber
     */
    void removeEntitySubscriber(EntitySubscriber subscriber);

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
     * @param <T>     Command class
     * @param <C>     Result class
     * @return {@link CompletableFuture} with command's result
     */
    <T extends Command<C>, C> CompletableFuture<C> publish(T command);

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
     * <p>
     * <p>Example:</p>
     * <p>
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
    default <E extends Entity> ResultSet<EntityHandle<E>> query(Class<E> klass, Query<EntityHandle<E>> query,
                                                                QueryOptions queryOptions) {
        return getIndexEngine().getIndexedCollection(klass).retrieve(query, queryOptions);
    }

    /**
     * @return Repository's current timestamp
     */
    HybridTimestamp getTimestamp();
}
