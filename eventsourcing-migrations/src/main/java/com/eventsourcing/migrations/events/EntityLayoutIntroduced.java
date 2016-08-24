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
import com.eventsourcing.index.Index;
import com.eventsourcing.index.SimpleIndex;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.LayoutName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.Optional;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;
import static com.eventsourcing.index.IndexEngine.IndexFeature.UNIQUE;

@Accessors(fluent = true)
@LayoutName("rfc.eventsourcing.com/spec:8/EMT/#EntityLayoutIntroduced")
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:8/EMT/", revision = "July 22, 2016")
public class EntityLayoutIntroduced extends StandardEvent {
    @Getter
    private final byte[] fingerprint;
    @Getter
    private final Optional<Layout<?>> layout;

    @Builder
    public EntityLayoutIntroduced(HybridTimestamp timestamp, byte[] fingerprint,
                                  Optional<Layout<?>> layout) {
        super(timestamp);
        this.fingerprint = fingerprint;
        this.layout = layout;
    }
    public EntityLayoutIntroduced(byte[] fingerprint,
                                  Optional<Layout<?>> layout) {
        this(null, fingerprint, layout);
    }

    @Index({EQ, UNIQUE})
    public static SimpleIndex<EntityLayoutIntroduced, byte[]> FINGERPRINT = EntityLayoutIntroduced::fingerprint;
}
