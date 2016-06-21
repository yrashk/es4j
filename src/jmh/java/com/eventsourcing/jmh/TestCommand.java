/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;

import java.util.stream.Stream;

public class TestCommand extends StandardCommand<String> {
    @Override
    public Stream<? extends Event> events(Repository repository) {
        return Stream.of((Event)new TestEvent().string("test"));
    }

    @Override
    public String onCompletion() {
        return "hello, world";
    }
}
