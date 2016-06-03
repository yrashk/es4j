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
public class ItemRemovedFromOrder extends Event {
    @Getter @Setter @NonNull
    private UUID itemId;

    @Index({EQ})
    public static final SimpleAttribute<ItemRemovedFromOrder, UUID> LINE_ID = new SimpleAttribute<ItemRemovedFromOrder, UUID>(
            "itemId") {
        public UUID getValue(ItemRemovedFromOrder itemRemovedFromOrder, QueryOptions queryOptions) {
            return itemRemovedFromOrder.itemId();
        }
    };

}
