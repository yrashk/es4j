/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.events;

import com.eventsourcing.StandardEntity;
import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Index;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.SimpleIndex;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.layout.LayoutName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;
import static com.eventsourcing.index.IndexEngine.IndexFeature.GT;
import static com.eventsourcing.index.IndexEngine.IndexFeature.LT;

/**
 * This event signifies deletion of a referenced instance.
 */
@Accessors(fluent = true)
@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("rfc.eventsourcing.com/spec:3/CEP/#Deleted")
public class Deleted extends StandardEvent {
    @Getter
    final UUID reference;

    @Index
    public final static SimpleIndex<Deleted, UUID> ID = SimpleIndex.as(StandardEntity::uuid);

    public final static SimpleIndex<Deleted, UUID> REFERENCE_ID = SimpleIndex.as(Deleted::reference);

    @Index({EQ, LT, GT})
    public final static SimpleIndex<Deleted, HybridTimestamp> TIMESTAMP = SimpleIndex.as(StandardEntity::timestamp);

    @LayoutConstructor
    public Deleted(UUID reference) {
        this.reference = reference;
    }

    @Builder
    public Deleted(UUID reference, HybridTimestamp timestamp) {
        super(timestamp);
        this.reference = reference;
    }
}
