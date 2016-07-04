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
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.Indexing;
import com.eventsourcing.layout.LayoutName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.UNIQUE;

@Accessors(fluent = true)
@LayoutName("rfc.eventsourcing.com/spec:9/RIG/#EventCausalityEstablished")
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:9/RIG/", revision = "July 22, 2016")
public class EventCausalityEstablished extends StandardEvent {
    @Getter(onMethod = @_(@Index))
    private final UUID event;
    @Getter(onMethod = @_(@Index))
    private final UUID command;

    @Builder
    public EventCausalityEstablished(HybridTimestamp timestamp, UUID event, UUID command) {
        super(timestamp);
        this.event = event;
        this.command = command;
    }

    public static Attribute<EventCausalityEstablished, UUID> EVENT = Indexing.getAttribute
            (EventCausalityEstablished.class, "event");

    public static Attribute<EventCausalityEstablished, UUID> COMMAND = Indexing.getAttribute
            (EventCausalityEstablished.class, "command");
}
