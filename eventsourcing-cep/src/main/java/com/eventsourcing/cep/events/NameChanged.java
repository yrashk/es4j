/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.events;

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

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

/**
 * This event signifies the name change for a referenced instance.
 */
@Accessors(fluent = true)
@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("http://rfc.eventsourcing.com/spec:3/CEP/#NameChanged")
public class NameChanged extends StandardEvent {
    @Getter
    final UUID reference;
    @Getter
    final String name;

    @LayoutConstructor
    public NameChanged(UUID reference, String name) {
        this.reference = reference;
        this.name = name;
    }

    @Builder
    public NameChanged(UUID reference, String name, HybridTimestamp timestamp) {
        super(timestamp);
        this.reference = reference;
        this.name = name;
    }

    public static SimpleIndex<NameChanged, UUID> REFERENCE_ID = (object, queryOptions) -> object.reference();

    public static SimpleIndex<NameChanged, String> NAME = (object, queryOptions) -> object.name();

    @Index({LT, GT, EQ})
    public static SimpleIndex<NameChanged, HybridTimestamp> TIMESTAMP = (object, queryOptions) -> object.timestamp();
}
