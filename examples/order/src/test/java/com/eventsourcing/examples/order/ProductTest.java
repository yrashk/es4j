/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        assertEquals(product.name(), "Widget");
        assertEquals(product.price(), new BigDecimal("100.50"));
    }

    @Test @SneakyThrows
    public void renaming() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        repository.publish(Rename.builder().id(product.getId()).name("New Widget").build()).get();
        assertEquals(product.name(), "New Widget");
    }

    @Test @SneakyThrows
    public void changingPrice() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        repository.publish(ChangePrice.builder().id(product.getId()).price(new BigDecimal("699.99")).build()).get();
        assertEquals(product.price(), new BigDecimal("699.99"));
    }

}