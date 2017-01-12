/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Repository;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.queries.ModelQueries;
import com.googlecode.cqengine.resultset.ResultSet;
import foodsourcing.events.OrderConfirmed;
import foodsourcing.events.OrderPlaced;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.eventsourcing.queries.QueryFactory.equal;
import static foodsourcing.Order.Status.CONFIRMED;

public class Order {

    @Value
    @Accessors(fluent = true)
    public static class Item {
        UUID menuItem;
        int quantity;

        @LayoutConstructor
        public Item(UUID menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }

        public Item(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem.getId();
            this.quantity = quantity;
        }

    }

    @Getter
    private final Repository repository;
    @Getter
    private final UUID id;

    @Override public boolean equals(Object obj) {
        return obj instanceof Restaurant && getId().equals(((Restaurant) obj).getId());
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    public BigDecimal price() {
        Optional<OrderPlaced> orderPlaced =
                ModelQueries.lookup(repository, OrderPlaced.class, OrderPlaced.ID, id);
        return orderPlaced.get().items().stream()
                          .map(i -> MenuItem.lookup(repository, i.menuItem())
                                            .get().price().multiply(new BigDecimal(i.quantity())))
                          .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    enum Status {
        PLACED, CONFIRMED
    }

    public Status status() {
        try (ResultSet<EntityHandle<OrderConfirmed>> resultSet = repository
                .query(OrderConfirmed.class, equal(OrderConfirmed.REFERENCE, getId()))) {
            return resultSet.isEmpty() ? Status.PLACED : CONFIRMED;
        }
    }

    protected Order(Repository repository, UUID id) {
        this.repository = repository;
        this.id = id;
    }

    public static Optional<Order> lookup(Repository repository, UUID id) {
        Optional<OrderPlaced> orderPlaced =
                ModelQueries.lookup(repository, OrderPlaced.class, OrderPlaced.ID, id);
        if (orderPlaced.isPresent()) {
            return Optional.of(new Order(repository, id));
        } else {
            return Optional.empty();
        }
    }
}
