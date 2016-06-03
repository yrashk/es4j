/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class ItemQuantityAdjusted extends Event {
    @Getter @Setter @NonNull
    private UUID itemId;

    @Getter @Setter @NonNull
    private Integer quantity;

    @Index({EQ})
    public static final SimpleAttribute<ItemQuantityAdjusted, UUID> ITEM_ID = new SimpleAttribute<ItemQuantityAdjusted, UUID>(
            "itemId") {
        public UUID getValue(ItemQuantityAdjusted itemQuantityAdjusted, QueryOptions queryOptions) {
            return itemQuantityAdjusted.itemId();
        }
    };
}
