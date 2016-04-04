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

import org.eventchain.Command;
import org.eventchain.Event;
import org.eventchain.Repository;
import org.eventchain.examples.order.Order;
import org.eventchain.examples.order.events.OrderCreated;

import java.util.stream.Stream;

public class CreateOrder extends Command<Order> {

    private Repository repository;
    private OrderCreated orderCreated;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        this.repository = repository;
        this.orderCreated = new OrderCreated();
        return Stream.of(orderCreated);
    }
    @Override
    public Order onCompletion() {
        return Order.lookup(repository, orderCreated.uuid()).get();
    }
}
