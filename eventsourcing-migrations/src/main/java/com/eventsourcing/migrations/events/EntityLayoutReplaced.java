/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.migrations.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.Indexing;
import com.eventsourcing.layout.LayoutName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.UUID;

@Accessors(fluent = true)
@LayoutName("rfc.eventsourcing.com/spec:8/EMT/#EntityLayoutReplaced")
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:8/EMT/", revision = "July 22, 2016")
public class EntityLayoutReplaced extends StandardEvent {
    @Getter(onMethod = @_(@Index)) @Setter
    private byte[] fingerprint;
    @Getter(onMethod = @_(@Index)) @Setter
    private UUID replacement;

    public static Attribute<EntityLayoutReplaced, byte[]> FINGERPRINT = Indexing.getAttribute
            (EntityLayoutReplaced.class, "fingerprint");

    public static Attribute<EntityLayoutReplaced, UUID> REPLACEMENT = Indexing.getAttribute
            (EntityLayoutReplaced.class, "replacement");

}
