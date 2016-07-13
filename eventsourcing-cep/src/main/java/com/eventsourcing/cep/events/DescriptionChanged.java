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
 * This event signifies a description change for a referenced instance.
 */
@Accessors(fluent = true)
@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("http://rfc.eventsourcing.com/spec:3/CEP/#DescriptionChanged")
public class DescriptionChanged extends StandardEvent {
    @Getter
    final UUID reference;
    @Getter
    final String description;

    @LayoutConstructor
    public DescriptionChanged(UUID reference, String description) {
        this.reference = reference;
        this.description = description;
    }

    @Builder
    public DescriptionChanged(UUID reference, String description, HybridTimestamp timestamp) {
        super(timestamp);
        this.reference = reference;
        this.description = description;
    }

    public static SimpleIndex<DescriptionChanged, UUID> REFERENCE_ID = (object, queryOptions) -> object.reference();

    public static SimpleIndex<DescriptionChanged, String> DESCRIPTION = (object, queryOptions) -> object.description();

    @Index({LT, GT, EQ})
    public static SimpleIndex<DescriptionChanged, HybridTimestamp> TIMESTAMP = (object, queryOptions) -> object
            .timestamp();
}
