/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.layout.LayoutIgnore;
import com.eventsourcing.repository.LockProvider;

/**
 * Thin implementation of {@link Command}
 * @param <R> result type
 * @param <S> state type
 */
public abstract class StandardCommand<R, S> extends StandardEntity<Command<R, S>> implements Command<R, S> {

    /**
     * Returns a stream of events that should be recorded. By default, an empty stream returned.
     *
     * @param repository Configured repository
     * @return stream of events
     * @throws Exception if the command is to be rejected, an exception has to be thrown. In this case, no events will
     *                   be recorded
     */

    public EventStream<S> events(Repository repository) throws Exception {
        return EventStream.empty();
    }

    @Override public EventStream<S> events(Repository repository, LockProvider lockProvider) throws
                                                                                                       Exception {
        return events(repository);
    }

    /**
     * Once all events are recorded, this callback will be invoked.
     *
     * By default, it calls {@link #onCompletion(Object, Repository)}
     *
     * @param state
     * @param repository
     * @param lockProvider
     * @return result
     */
    @Override public R onCompletion(S state, Repository repository, LockProvider lockProvider) {
        return onCompletion(state, repository);
    }

    /**
     * By default, calls {@link #onCompletion(Object)}
     *
     * @param state
     * @param repository
     * @return result
     */
    @LayoutIgnore
    public R onCompletion(S state, Repository repository) {
        return onCompletion(state);
    }

    /**
     * By default, it calls {@link #onCompletion()}
     *
     * @param state
     * @return result
     */
    @LayoutIgnore
    public R onCompletion(S state) {
        return onCompletion();
    }

    /**
     * By default, does nothing (returns <code>null</code>)
     * @return result
     */
    @LayoutIgnore
    public R onCompletion() {
        return null;
    }
}
