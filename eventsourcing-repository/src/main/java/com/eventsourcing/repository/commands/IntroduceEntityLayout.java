/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.migrations.events.EntityLayoutIntroduced;
import lombok.Value;

import java.util.Optional;

@Value
public class IntroduceEntityLayout extends StandardCommand<Void, Void> {

    private final byte[] fingerprint;
    private final Optional<Layout<?>> layout;


    @Override public EventStream<Void> events(Repository repository) throws Exception {
        return EventStream.of(new EntityLayoutIntroduced(fingerprint, layout));
    }
}
