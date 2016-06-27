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
import com.eventsourcing.examples.order.Order;
import com.eventsourcing.examples.order.events.ProductAddedToOrder;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class AddProductToOrder extends StandardCommand<Order.Item, ProductAddedToOrder> {

    @Getter @Setter @NonNull
    private UUID orderId;

    @Getter @Setter @NonNull
    private UUID productId;

    @Getter @Setter @NonNull
    private Integer quantity;

    @Override
    public EventStream<ProductAddedToOrder> events(Repository repository) throws Exception {
        ProductAddedToOrder addedToOrder = new ProductAddedToOrder(orderId, productId, quantity);
        return EventStream.ofWithState(addedToOrder, addedToOrder);
    }

    @Override
    public Order.Item onCompletion(ProductAddedToOrder productAddedToOrder, Repository repository) {
        return Order.lookup(repository, orderId).get().items().stream().
                filter(item -> item.id().equals(productAddedToOrder.uuid())).findFirst().get();
    }
}
