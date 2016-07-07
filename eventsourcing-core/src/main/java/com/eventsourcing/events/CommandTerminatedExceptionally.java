/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.layout.LayoutName;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
@LayoutName("rfc.eventsourcing.com/spec:9/RIG/#CommandTerminatedExceptionally")
public class CommandTerminatedExceptionally extends StandardEvent {

    @Index
    public static Attribute<CommandTerminatedExceptionally, UUID> ID = new
            SimpleAttribute<CommandTerminatedExceptionally, UUID>("id") {
        @Override public UUID getValue(CommandTerminatedExceptionally object, QueryOptions queryOptions) {
            return object.uuid();
        }
    };
}
