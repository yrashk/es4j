package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.Order;
import com.eventsourcing.examples.order.events.OrderCreated;

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
