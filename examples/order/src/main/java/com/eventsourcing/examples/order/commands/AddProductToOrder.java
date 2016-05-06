/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.Order;
import com.eventsourcing.examples.order.events.ProductAddedToOrder;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class AddProductToOrder extends Command<Order.Item> {

    @Getter @Setter @NonNull
    private UUID orderId;

    @Getter @Setter @NonNull
    private UUID productId;

    @Getter @Setter @NonNull
    private Integer quantity;

    private Repository repository;
    private ProductAddedToOrder addedToOrder;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
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
