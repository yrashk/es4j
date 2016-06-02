/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Model;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.events.ProductCreated;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.UUID;

import static com.googlecode.cqengine.query.QueryFactory.equal;

@Accessors(fluent = true)
public class Product implements Model, NameProtocol, PriceProtocol {

    @Getter
    private UUID id;

    @Getter @Accessors(fluent = false)
    private Repository repository;

    public Product(Repository repository, UUID id) {
        this.repository = repository;
        this.id = id;
    }

    public static Optional<Product> lookup(Repository repository, UUID id) {
        try (ResultSet<EntityHandle<ProductCreated>> resultSet = repository
                .query(ProductCreated.class, equal(ProductCreated.ID, id))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new Product(repository, resultSet.uniqueResult().uuid()));
        }
    }

}
