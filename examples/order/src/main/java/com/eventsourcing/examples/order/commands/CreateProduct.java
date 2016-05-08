/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.Product;
import com.eventsourcing.examples.order.events.NameChanged;
import com.eventsourcing.examples.order.events.PriceChanged;
import com.eventsourcing.examples.order.events.ProductCreated;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.stream.Stream;

@Accessors(fluent = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class CreateProduct extends Command<Product> {

    @Getter @Setter @NonNull
    private String name;

    @Getter @Setter @NonNull
    private BigDecimal price;

    private Repository repository;
    private ProductCreated productCreated;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        this.repository = repository;
        productCreated = new ProductCreated();
        NameChanged nameChanged = new NameChanged(productCreated.uuid(), name);
        PriceChanged priceChanged = new PriceChanged(productCreated.uuid(), price);
        return Stream.of(productCreated, nameChanged, priceChanged);
    }

    @Override
    public Product onCompletion() {
        return Product.lookup(repository, productCreated.uuid()).get();
    }
}
