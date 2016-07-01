/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.examples.order.events.ItemQuantityAdjusted;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
public class AdjustItemQuantity extends StandardCommand<Void, Void> {
    @Getter @NonNull
    private final UUID itemId;

    @Getter @NonNull
    private final Integer quantity;

    @Builder
    public AdjustItemQuantity(UUID itemId, Integer quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    @Override
    public EventStream<Void> events(Repository repository) throws Exception {
        return EventStream.of(ItemQuantityAdjusted.builder().itemId(itemId).quantity(quantity).build());
    }
}
