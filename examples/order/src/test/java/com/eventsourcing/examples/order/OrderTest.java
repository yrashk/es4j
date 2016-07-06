/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order;

import com.eventsourcing.examples.order.commands.*;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.*;

public class OrderTest extends EventsourcingTest {


    @Test
    @SneakyThrows
    public void create() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        assertTrue(order.items().isEmpty());
    }

    @Test
    @SneakyThrows
    public void cancel() {
        Order order = repository.publish(CreateOrder.builder().build()).get();
        assertEquals(order.status(), Order.Status.OPEN);
        repository.publish(CancelOrder.builder().id(order.getId()).build()).get();
        assertEquals(order.status(), Order.Status.CANCELLED);
    }

    @Test
    @SneakyThrows
    public void addProduct() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        AddProductToOrder addProductToOrder = AddProductToOrder.builder().orderId(order.getId())
                                                   .productId(product.getId()).quantity(10).build();
        Order.Item item = repository.publish(addProductToOrder).get();
        assertFalse(order.items().isEmpty());
        assertEquals(order.items().get(0).getId(), item.getId());
        assertEquals(order.items().get(0).getProduct().getId(), product.getId());
        assertEquals(order.items().get(0).getQuantity(), 10);
        Order.Item item1 = repository.publish(AddProductToOrder.builder().orderId(order.getId()).productId(product.getId())
                                                               .quantity(10).build()).get();
        assertEquals(order.items().size(), 2);
        assertNotEquals(item1.getId(), item.getId());
    }

    @Test
    @SneakyThrows
    public void removeLine() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        Order.Item item = repository.publish(AddProductToOrder.builder().orderId(order.getId()).productId(product.getId())
                                                              .quantity(10).build()).get();
        Order.Item item1 = repository.publish(AddProductToOrder.builder().orderId(order.getId()).productId(product.getId())
                                                              .quantity(10).build()).get();

        repository.publish(RemoveItemFromOrder.builder().itemId(item.getId()).build()).get();
        assertEquals(order.items().size(), 1);
        assertEquals(order.items().get(0).getId(), item1.getId());
    }

    @Test
    @SneakyThrows
    public void adjustQuantity() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        Order.Item item = repository.publish(AddProductToOrder.builder().orderId(order.getId()).productId(product.getId())
                                                              .quantity(10).build()).get();
        repository.publish(AdjustItemQuantity.builder().itemId(item.getId()).quantity(20).build()).get();
        assertEquals(order.items().get(0).getQuantity(), 20);
    }
}
