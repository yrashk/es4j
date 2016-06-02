package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.events.ItemRemovedFromOrder;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class RemoveItemFromOrder extends Command<Void> {

    @Getter @Setter @NonNull
    private UUID itemId;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.of(new ItemRemovedFromOrder(itemId));
    }
}
