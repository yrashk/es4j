/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.events;

import com.eventsourcing.StandardEntity;
import com.eventsourcing.StandardEvent;
import com.eventsourcing.index.SimpleIndex;
import com.eventsourcing.layout.LayoutName;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
@LayoutName("rfc.eventsourcing.com/spec:9/RIG/#CommandTerminatedExceptionally")
public class CommandTerminatedExceptionally extends StandardEvent {

    public static SimpleIndex<CommandTerminatedExceptionally, UUID> ID = StandardEntity::uuid;
}
