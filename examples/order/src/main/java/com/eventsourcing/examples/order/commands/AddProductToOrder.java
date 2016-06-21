/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.examples.order.Order;
import com.eventsourcing.examples.order.events.ProductAddedToOrder;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class AddProductToOrder extends StandardCommand<Order.Item> {

    @Getter @Setter @NonNull
    private UUID orderId;

    @Getter @Setter @NonNull
    private UUID productId;

    @Getter @Setter @NonNull
    private Integer quantity;

    private Repository repository;
    private ProductAddedToOrder addedToOrder;

    @Override
    public Stream<? extends Event> events(Repository repository) throws Exception {
        this.repository = repository;
        addedToOrder = new ProductAddedToOrder(orderId, productId, quantity);
        return Stream.of(addedToOrder);
    }

    @Override
    public Order.Item onCompletion() {
        return Order.lookup(repository, orderId).get().items().stream().
                filter(item -> item.id().equals(addedToOrder.uuid())).findFirst().get();
    }
}
