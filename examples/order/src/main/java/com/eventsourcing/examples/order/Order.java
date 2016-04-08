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
package com.eventsourcing.examples.order;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.events.*;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;

@Accessors(fluent = true)
public class Order {

    @Getter
    private UUID id;

    @Getter @Accessors(fluent = false)
    private Repository repository;

    public Order(Repository repository, UUID id) {
        this.repository = repository;
        this.id = id;
    }

    @AllArgsConstructor
    public static class Item {
        @Getter
        private UUID id;
        @Getter
        private Product product;
        @Getter @Setter(AccessLevel.PACKAGE)
        private int quantity;


    }

    enum Status {
        OPEN, CANCELLED
    }

    public Status status() {
        try (ResultSet<EntityHandle<OrderCancelled>> resultSet =
                     repository.query(OrderCancelled.class, equal(OrderCancelled.REFERENCE_ID, id))) {
            return resultSet.isEmpty() ? Status.OPEN : Status.CANCELLED;
        }
    }

    public List<Item> items() {
        try (ResultSet<EntityHandle<ProductAddedToOrder>> resultSet = repository.query(ProductAddedToOrder.class,
                and(equal(ProductAddedToOrder.ORDER_ID, id),
                    not(existsIn(repository.getIndexEngine().getIndexedCollection(ItemRemovedFromOrder.class),
                            ProductAddedToOrder.ID, ItemRemovedFromOrder.LINE_ID))),
                queryOptions(orderBy(ascending(ProductAddedToOrder.TIMESTAMP))))) {
            Map<UUID, Item> items = StreamSupport.stream(resultSet.spliterator(), false).
                    map(addition -> addition.get().get()).
                    map(addition -> new Item(addition.uuid(), Product.lookup(repository, addition.productId()).get(), addition.quantity())).
                    collect(Collectors.toMap(Item::id, Function.identity()));
            try (ResultSet<EntityHandle<ItemQuantityAdjusted>> adjustments = repository.query(ItemQuantityAdjusted.class, in(ItemQuantityAdjusted.ITEM_ID, items.keySet()))) {
                adjustments.forEach(adj -> {
                    ItemQuantityAdjusted itemQuantityAdjusted = adj.get().get();
                    items.get(itemQuantityAdjusted.itemId()).quantity(itemQuantityAdjusted.quantity());
                });
            }
            return items.values().stream().collect(Collectors.toList());
        }
    }

    public static Optional<Order> lookup(Repository repository, UUID id) {
        try (ResultSet<EntityHandle<OrderCreated>> resultSet = repository.query(OrderCreated.class, equal(OrderCreated.ID, id))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new Order(repository, resultSet.uniqueResult().uuid()));
        }
    }
}
