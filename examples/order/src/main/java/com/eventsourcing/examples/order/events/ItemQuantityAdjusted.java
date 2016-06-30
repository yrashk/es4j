/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

@Accessors(fluent = true)
public class ItemQuantityAdjusted extends StandardEvent {
    @Getter @NonNull
    private final UUID itemId;

    @Getter @NonNull
    private final Integer quantity;

    @Index({EQ})
    public static final SimpleAttribute<ItemQuantityAdjusted, UUID> ITEM_ID = new SimpleAttribute<ItemQuantityAdjusted, UUID>(
            "itemId") {
        public UUID getValue(ItemQuantityAdjusted itemQuantityAdjusted, QueryOptions queryOptions) {
            return itemQuantityAdjusted.itemId();
        }
    };

    @Builder
    public ItemQuantityAdjusted(HybridTimestamp timestamp, UUID itemId, Integer quantity) {
        super(timestamp);
        this.itemId = itemId;
        this.quantity = quantity;
    }
}
