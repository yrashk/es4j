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

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

/**
 * Journal is the storage of all events and commands registered
 * through Eventchain.
 */
public interface Journal extends Service {

    Listener DEFAULT_LISTENER = new Listener() {};

    /**
     * Set repository. Should be done before invoking {@link #startAsync()}
     *
     * @param repository
     * @throws IllegalStateException if called after the service is started
     */
    void setRepository(Repository repository) throws IllegalStateException;

    /**
     * Journal <code>command</code> in repository <code>repository</code>,
     * with a default (no-op) listener ({@link #DEFAULT_LISTENER})
     *
     * See more details at {@link #journal(Command, Listener)}
     *
     * @param command
     * @return number of events processed
     */
    default long journal(Command<?> command) {
        return journal(command, DEFAULT_LISTENER);
    }

    /**
     * Journal <code>command</code> in repository <code>repository</code>,
     * with a custom listener.
     *
     * Journal implementation should not make events visible to any other
     * readers until all events and the command have been persisted.
     *
     * @param command
     * @param listener
     * @return number of events processed
     */
    default long journal(Command<?> command, Listener listener) {
        return journal(command, listener, new MemoryLockProvider());
    }

    /**
     * Journal <code>command</code> in repository <code>repository</code>,
     * with a custom listener and a lock provider.
     *
     * @param command
     * @param listener
     * @param lockProvider
     * @return number of events processed
     */
    long journal(Command<?> command, Listener listener, LockProvider lockProvider);

    /**
     * Retrieves a command or event by UUID
     *
     * @param uuid
     * @param <T>
     * @return Empty {@link Optional} if neither a command nor an event are found by <code>uuid</code>
     */
    <T extends Entity> Optional<T> get(UUID uuid);

    /**
     * Iterate over commands of a specific type (through {@code EntityHandler<T>})
     * @param klass
     * @param <T>
     * @return iterator
     */
    <T extends Command<?>> Iterator<EntityHandle<T>> commandIterator(Class<T> klass);
    /**
     * Iterate over events of a specific type (through {@code EntityHandler<T>})
     * @param klass
     * @param <T>
     * @return iterator
     */
    <T extends Event> Iterator<EntityHandle<T>> eventIterator(Class<T> klass);

    /**
     * Removes everything from the journal.
     *
     * <b>Use with caution</b>: the data will be lost irrevocably
     */
    void clear();

    /**
     * Returns the count of entities of specified type
     *
     * @param klass
     * @param <T>
     * @return
     */
    <T extends Entity> long size(Class<T> klass);

    /**
     * Returns true if there are no entities of specified type stored
     *
     * @param klass
     * @param <T>
     * @return
     */
    <T extends Entity> boolean isEmpty(Class<T> klass);

    /**
     * Journalling listener. Useful for observing progress.
     */
    interface Listener {
        /**
         * Called when a new event is received from the stream and
         * persisted into the journal. Note that at this point the event
         * should not be available until the entire journalling operation has
         * been completed and {@link #onCommit()} has been called.
         * @param event
         */
        default void onEvent(Event event) {}

        /**
         * Called when all events and the command were persisted into the journal.
         * At this point, all records have been committed and are visible to all
         * other readers.
         */
        default void onCommit() {}
    }
}
