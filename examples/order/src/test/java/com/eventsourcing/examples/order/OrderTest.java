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
        repository.publish(CancelOrder.builder().id(order.id()).build()).get();
        assertEquals(order.status(), Order.Status.CANCELLED);
    }

    @Test
    @SneakyThrows
    public void addProduct() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        AddProductToOrder addProductToOrder = AddProductToOrder.builder().orderId(order.id())
                                                   .productId(product.id()).quantity(10).build();
        Order.Item item = repository.publish(addProductToOrder).get();
        assertFalse(order.items().isEmpty());
        assertEquals(order.items().get(0).id(), item.id());
        assertEquals(order.items().get(0).product().id(), product.id());
        assertEquals(order.items().get(0).quantity(), 10);
        Order.Item item1 = repository.publish(AddProductToOrder.builder().orderId(order.id()).productId(product.id())
                                                               .quantity(10).build()).get();
        assertEquals(order.items().size(), 2);
        assertNotEquals(item1.id(), item.id());
    }

    @Test
    @SneakyThrows
    public void removeLine() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        Order.Item item = repository.publish(AddProductToOrder.builder().orderId(order.id()).productId(product.id())
                                                              .quantity(10).build()).get();
        Order.Item item1 = repository.publish(AddProductToOrder.builder().orderId(order.id()).productId(product.id())
                                                              .quantity(10).build()).get();

        repository.publish(RemoveItemFromOrder.builder().itemId(item.id()).build()).get();
        assertEquals(order.items().size(), 1);
        assertEquals(order.items().get(0).id(), item1.id());
    }

    @Test
    @SneakyThrows
    public void adjustQuantity() {
        CreateProduct widget = CreateProduct.builder().name("Widget").price(new BigDecimal("100.50")).build();
        Product product = repository.publish(widget).get();
        Order order = repository.publish(CreateOrder.builder().build()).get();
        Order.Item item = repository.publish(AddProductToOrder.builder().orderId(order.id()).productId(product.id())
                                                              .quantity(10).build()).get();
        repository.publish(AdjustItemQuantity.builder().itemId(item.id()).quantity(20).build()).get();
        assertEquals(order.items().get(0).quantity(), 20);
    }
}
