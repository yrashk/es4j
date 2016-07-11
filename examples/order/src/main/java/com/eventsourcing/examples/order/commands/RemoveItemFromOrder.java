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
import com.eventsourcing.examples.order.events.ItemRemovedFromOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
public class RemoveItemFromOrder extends StandardCommand<Void, Void> {

    @Getter @NonNull
    private final UUID itemId;

    @Builder
    public RemoveItemFromOrder(UUID itemId) {
        this.itemId = itemId;
    }

    @Override
    public EventStream<Void> events() throws Exception {
        return EventStream.of(ItemRemovedFromOrder.builder().itemId(itemId).build());
    }
}
