package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.events.OrderCancelled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class CancelOrder extends Command<Void> {

    @Getter @Setter
    private UUID id;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.of(new OrderCancelled(id));
    }
}
