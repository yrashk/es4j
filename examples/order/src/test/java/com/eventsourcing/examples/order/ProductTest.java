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

import com.eventsourcing.examples.order.commands.ChangePrice;
import com.eventsourcing.examples.order.commands.CreateProduct;
import com.eventsourcing.examples.order.commands.Rename;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class ProductTest extends EventsourcingTest {

    @Test @SneakyThrows
    public void create() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        assertEquals(product.name(), "Widget");
        assertEquals(product.price(), new BigDecimal("100.50"));
    }

    @Test @SneakyThrows
    public void renaming() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        repository.publish(new Rename(product.id(), "New Widget")).get();
        assertEquals(product.name(), "New Widget");
    }

    @Test @SneakyThrows
    public void changingPrice() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        repository.publish(new ChangePrice(product.id(), new BigDecimal("699.99"))).get();
        assertEquals(product.price(), new BigDecimal("699.99"));
    }

}