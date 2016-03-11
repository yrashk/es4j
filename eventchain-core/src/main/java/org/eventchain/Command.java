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

import org.eventchain.layout.LayoutIgnore;

import java.util.stream.Stream;

/**
 *  Command is a request for changes in the domain. Unlike an event,
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

    public Stream<Event> events(Repository repository) {
        return Stream.empty();
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
