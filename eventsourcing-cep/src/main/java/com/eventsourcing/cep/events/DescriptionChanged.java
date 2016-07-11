/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.layout.LayoutName;
import com.googlecode.cqengine.query.option.QueryOptions;
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

    @Index
    public static SimpleAttribute<DescriptionChanged, UUID> REFERENCE_ID = new SimpleAttribute<DescriptionChanged, UUID>
            ("reference_id") {
        @Override public UUID getValue(DescriptionChanged descriptionChanged, QueryOptions queryOptions) {
            return descriptionChanged.reference();
        }
    };

    @Index({EQ})
    public static SimpleAttribute<DescriptionChanged, String> DESCRIPTION = new SimpleAttribute<DescriptionChanged, String>
            ("description") {
        @Override public String getValue(DescriptionChanged descriptionChanged, QueryOptions queryOptions) {
            return descriptionChanged.description();
        }
    };

    @Index({LT, GT, EQ})
    public static SimpleAttribute<DescriptionChanged, HybridTimestamp> TIMESTAMP = new SimpleAttribute<DescriptionChanged, HybridTimestamp>
            ("timestamp") {
        @Override public HybridTimestamp getValue(DescriptionChanged descriptionChanged, QueryOptions queryOptions) {
            return descriptionChanged.timestamp();
        }
    };
}
