package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.events.NameChanged;
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
public class Rename extends Command<String> {
    @Getter @Setter
    private UUID id;

    @Getter @Setter
    private String name;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.of(new NameChanged(id, name));
    }

    @Override
    public String onCompletion() {
        return name;
    }
}
