/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.layout.LayoutIgnore;

import java.util.stream.Stream;

/**
 * Command is a request for changes in the domain. Unlike an event,
 * it is not a statement of fact as it might be rejected.
 *
 * For example, ConfirmOrder command may or may not result in an
 * OrderConfirmed event being produced.
 *
 * @param <R> result type
 */
public abstract class Command<R> extends Entity {

    /**
     * Returns a stream of events that should be recorded. By default, an empty stream returned.
     *
     * @param repository Configured repository
     * @return stream of events
     * @throws Exception if the command is to be rejected, an exception has to be thrown. In this case, no events will
     *         be recorded
     */

    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.empty();
    }

    /**
     * Returns a stream of events that should be recorded. By default, an empty stream returned.
     *
     * This version of the function receives a {@link LockProvider} if one is needed. {@link DisruptorCommandConsumer}
     * will pass a special "tracking" provider that will release the locks in two situations:
     *
     * <ul>
     *     <li>{@link #events(Repository, LockProvider)} threw an exception</li>
     *     <li>{@link #onCompletion()} did not release any locks</li>
     * </ul>
     *
     * @param repository Configured repository
     * @param lockProvider Lock provider
     * @return stream of events
     * @throws Exception if the command is to be rejected, an exception has to be thrown. In this case, no events will
     *         be recorded
     */
    public Stream<Event> events(Repository repository, LockProvider lockProvider) throws Exception {
        return events(repository);
    }

    /**
     * Once all events are recorded, this callback will be invoked
     *
     * By default, it does nothing and it is meant to be overridden when necessary. For example,
     * if upon the successful recording of events an email has to be sent, this is the place
     * to do it.
     *
     * @return Result
     */
    @LayoutIgnore
    public R onCompletion() {
        return null;
    }
}
