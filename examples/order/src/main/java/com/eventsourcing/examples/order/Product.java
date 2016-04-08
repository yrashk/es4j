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
        try (ResultSet<EntityHandle<ProductCreated>> resultSet = repository.query(ProductCreated.class, equal(ProductCreated.ID, id))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new Product(repository, resultSet.uniqueResult().uuid()));
        }
    }

}
