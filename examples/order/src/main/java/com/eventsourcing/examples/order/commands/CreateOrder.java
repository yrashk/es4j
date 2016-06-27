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
import com.eventsourcing.examples.order.Order;
import com.eventsourcing.examples.order.events.OrderCreated;

public class CreateOrder extends StandardCommand<Order, OrderCreated> {

    @Override
    public EventStream<OrderCreated> events(Repository repository) throws Exception {
        OrderCreated orderCreated = new OrderCreated();
        return EventStream.ofWithState(orderCreated, orderCreated);
    }

    @Override
    public Order onCompletion(OrderCreated orderCreated, Repository repository) {
        return Order.lookup(repository, orderCreated.uuid()).get();
    }
}
