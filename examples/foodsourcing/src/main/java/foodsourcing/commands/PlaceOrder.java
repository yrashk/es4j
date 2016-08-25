/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import foodsourcing.Order;
import foodsourcing.events.OrderPlaced;
import lombok.Value;

import java.util.List;

@Value
public class PlaceOrder extends StandardCommand<OrderPlaced, Order> {

    private List<Order.Item> items;

    @Override public EventStream<OrderPlaced> events() throws Exception {
        OrderPlaced event = new OrderPlaced(items);
        return EventStream.ofWithState(event, event);
    }

    @Override public Order result(OrderPlaced orderPlaced, Repository repository) {
        return Order.lookup(repository, orderPlaced.uuid()).orElse(null);
    }
}
