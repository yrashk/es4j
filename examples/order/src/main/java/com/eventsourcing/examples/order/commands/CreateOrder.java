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
import lombok.Builder;

public class CreateOrder extends StandardCommand<OrderCreated, Order> {

    @Builder
    public CreateOrder() {}

    @Override
    public EventStream<OrderCreated> events() throws Exception {
        OrderCreated orderCreated = OrderCreated.builder().build();
        return EventStream.ofWithState(orderCreated, orderCreated);
    }

    @Override
    public Order result(OrderCreated orderCreated, Repository repository) {
        return Order.lookup(repository, orderCreated.uuid()).get();
    }
}
