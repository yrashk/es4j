/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.repository.LockProvider;

import java.util.stream.Stream;

/**
 * Thin implementation of {@link Command}
 * @param <R> result type
 */
public abstract class StandardCommand<R> extends StandardEntity<Command<R>> implements Command<R> {

    /**
     * Returns a stream of events that should be recorded. By default, an empty stream returned.
     *
     * @param repository Configured repository
     * @return stream of events
     * @throws Exception if the command is to be rejected, an exception has to be thrown. In this case, no events will
     *                   be recorded
     */

    public Stream<? extends Event> events(Repository repository) throws Exception {
        return Stream.empty();
    }

    @Override public Stream<? extends Event> events(Repository repository, LockProvider lockProvider) throws Exception {
        return events(repository);
    }

}
