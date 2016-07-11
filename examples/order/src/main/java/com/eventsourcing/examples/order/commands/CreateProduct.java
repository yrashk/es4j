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
import com.eventsourcing.examples.order.Product;
import com.eventsourcing.examples.order.events.NameChanged;
import com.eventsourcing.examples.order.events.PriceChanged;
import com.eventsourcing.examples.order.events.ProductCreated;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(fluent = true)
public class CreateProduct extends StandardCommand<ProductCreated, Product> {

    @Getter @NonNull
    private final String name;

    @Getter @NonNull
    private final BigDecimal price;

    @Builder
    public CreateProduct(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public EventStream<ProductCreated> events() throws Exception {
        ProductCreated productCreated = ProductCreated.builder().build();
        NameChanged nameChanged = NameChanged.builder().id(productCreated.uuid()).name(name).build();
        PriceChanged priceChanged = PriceChanged.builder().id(productCreated.uuid()).price(price).build();
        return EventStream.ofWithState(productCreated, productCreated, nameChanged, priceChanged);
    }

    @Override
    public Product result(ProductCreated productCreated, Repository repository) {
        return Product.lookup(repository, productCreated.uuid()).get();
    }
}
