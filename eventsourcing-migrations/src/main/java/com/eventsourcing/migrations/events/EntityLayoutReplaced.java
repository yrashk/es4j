/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.migrations.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleIndex;
import com.eventsourcing.layout.LayoutName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.UUID;

@Accessors(fluent = true)
@LayoutName("rfc.eventsourcing.com/spec:8/EMT/#EntityLayoutReplaced")
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:8/EMT/", revision = "July 22, 2016")
public class EntityLayoutReplaced extends StandardEvent {
    @Getter
    private final byte[] fingerprint;
    @Getter
    private final UUID replacement;

    @Builder
    public EntityLayoutReplaced(HybridTimestamp timestamp, byte[] fingerprint,
                                UUID replacement) {
        super(timestamp);
        this.fingerprint = fingerprint;
        this.replacement = replacement;
    }

    public static SimpleIndex<EntityLayoutReplaced, byte[]> FINGERPRINT = EntityLayoutReplaced::fingerprint;

    public static SimpleIndex<EntityLayoutReplaced, UUID> REPLACEMENT = EntityLayoutReplaced::replacement;

}
