/**
 * Copyright 2016 Eventchain team
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
package org.eventchain.examples.order.commands;

import lombok.*;
import lombok.experimental.Accessors;
import org.eventchain.Command;
import org.eventchain.Event;
import org.eventchain.Repository;
import org.eventchain.examples.order.Product;
import org.eventchain.examples.order.events.NameChanged;
import org.eventchain.examples.order.events.PriceChanged;
import org.eventchain.examples.order.events.ProductCreated;

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
