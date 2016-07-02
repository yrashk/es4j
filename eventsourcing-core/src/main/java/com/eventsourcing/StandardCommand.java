/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;

/**
 * Thin implementation of {@link Command}
 * @param <R> result type
 * @param <S> state type
 */
public abstract class StandardCommand<S, R> extends StandardEntity<Command<S, R>> implements Command<S, R> {

    public StandardCommand() { super(); }
    public StandardCommand(HybridTimestamp timestamp) {
        super(timestamp);
    }

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
     * By default, it calls {@link #result(Object, Repository)}
     *
     * @param state
     * @param repository
     * @param lockProvider
     * @return result
     */
    @Override public R result(S state, Repository repository, LockProvider lockProvider) {
        return result(state, repository);
    }

    /**
     * By default, calls {@link #result(Object)}
     *
     * @param state
     * @param repository
     * @return result
     */
    public R result(S state, Repository repository) {
        return result(state);
    }

    /**
     * By default, it calls {@link #result()}
     *
     * @param state
     * @return result
     */
    public R result(S state) {
        return result();
    }

    /**
     * By default, does nothing (returns <code>null</code>)
     * @return result
     */
    public R result() {
        return null;
    }
}
