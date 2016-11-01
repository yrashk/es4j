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
 * This event signifies undeletion of a referenced deletion.
 */
@Accessors(fluent = true)
@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("rfc.eventsourcing.com/spec:3/CEP/#Undeleted")
public class Undeleted extends StandardEvent {
    @Getter
    final UUID deleted;

    public final static SimpleIndex<Undeleted, UUID> DELETED_ID = SimpleIndex.as(Undeleted::deleted);

    @Index({EQ, LT, GT})
    public final static SimpleIndex<Undeleted, HybridTimestamp> TIMESTAMP = SimpleIndex.as(StandardEntity::timestamp);

    @LayoutConstructor
    public Undeleted(UUID deleted) {
        this.deleted = deleted;
    }

    @Builder
    public Undeleted(UUID deleted, HybridTimestamp timestamp) {
        super(timestamp);
        this.deleted = deleted;
    }
}
