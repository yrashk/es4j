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

import com.eventsourcing.examples.order.commands.*;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.*;

public class OrderTest extends EventsourcingTest {


    @Test
    @SneakyThrows
    public void create() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        Order order = repository.publish(new CreateOrder()).get();
        assertTrue(order.items().isEmpty());
    }

    @Test
    @SneakyThrows
    public void cancel() {
        Order order = repository.publish(new CreateOrder()).get();
        assertEquals(order.status(), Order.Status.OPEN);
        repository.publish(new CancelOrder(order.id())).get();
        assertEquals(order.status(), Order.Status.CANCELLED);
    }

    @Test
    @SneakyThrows
    public void addProduct() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        Order order = repository.publish(new CreateOrder()).get();
        Order.Item item = repository.publish(new AddProductToOrder(order.id(), product.id(), 10)).get();
        assertFalse(order.items().isEmpty());
        assertEquals(order.items().get(0).id(), item.id());
        assertEquals(order.items().get(0).product().id(),product.id());
        assertEquals(order.items().get(0).quantity(), 10);
        Order.Item item1 = repository.publish(new AddProductToOrder(order.id(), product.id(), 10)).get();
        assertEquals(order.items().size(), 2);
        assertNotEquals(item1.id(), item.id());
    }

    @Test
    @SneakyThrows
    public void removeLine() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        Order order = repository.publish(new CreateOrder()).get();
        Order.Item item = repository.publish(new AddProductToOrder(order.id(), product.id(), 10)).get();
        Order.Item item1 = repository.publish(new AddProductToOrder(order.id(), product.id(), 10)).get();
        repository.publish(new RemoveItemFromOrder(item.id())).get();
        assertEquals(order.items().size(), 1);
        assertEquals(order.items().get(0).id(), item1.id());
    }

    @Test
    @SneakyThrows
    public void adjustQuantity() {
        CreateProduct widget = new CreateProduct("Widget", new BigDecimal("100.50"));
        Product product = repository.publish(widget).get();
        Order order = repository.publish(new CreateOrder()).get();
        Order.Item item = repository.publish(new AddProductToOrder(order.id(), product.id(), 10)).get();
        repository.publish(new AdjustItemQuantity(item.id(), 20)).get();
        assertEquals(order.items().get(0).quantity(), 20);
    }
}
