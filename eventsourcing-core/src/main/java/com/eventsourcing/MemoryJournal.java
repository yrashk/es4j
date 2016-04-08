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
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.Deserializer;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.Serializer;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AbstractService;
import lombok.Getter;
import lombok.SneakyThrows;
import org.osgi.service.component.annotations.Component;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Memory-based {@link Journal} implementation. Not meant to be used in production.
 */
@Component(property = {"type=MemoryJournal"})
public class MemoryJournal extends AbstractService implements Journal {

    private Repository repository;

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    private Map<UUID, Command> commands = new HashMap<>();
    private Map<UUID, Event>   events = new HashMap<>();
    private Map<UUID, UUID>    eventCommands = new HashMap<>();

    @Override
    protected void doStart() {
        if (repository == null) {
            notifyFailed(new IllegalStateException("repository == null"));
        }
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }

    // We are using synchronized methods in this implementation to imitate
    // the transaction isolation guarantees promised by Journal#journal's
    // documentation

    @Override
    public synchronized long journal(Command<?> command, Journal.Listener listener, LockProvider lockProvider) throws Exception {
        Map<UUID, Event> events_ = new HashMap<>();
        Map<UUID, UUID>  eventCommands_ = new HashMap<>();
        EventConsumer eventConsumer = new EventConsumer(events_, eventCommands_, command, listener);

        Stream<Event> events;
        events = command.events(repository, lockProvider);

        long count;

        try {
            count = events.peek(eventConsumer).count();
        } catch (Exception e) {
            listener.onAbort(e);
            throw e;
        }

        this.events.putAll(events_);
        this.eventCommands.putAll(eventCommands_);

        Layout<Command> layout = new Layout<>((Class<Command>)command.getClass());
        Serializer<Command> serializer = new Serializer<>(layout);
        Deserializer<Command> deserializer = new Deserializer<>(layout);

        ByteBuffer buffer = serializer.serialize(command);
        buffer.rewind();
        deserializer.deserialize(command, buffer);


        commands.put(command.uuid(), command);

        listener.onCommit();
        return count;
    }

    @Override @SuppressWarnings("unchecked")
    public synchronized <T extends Entity> Optional<T> get(UUID uuid) {
        if (commands.containsKey(uuid)) {
            return Optional.of((T) commands.get(uuid));
        }
        if (events.containsKey(uuid)) {
            return Optional.of((T) events.get(uuid));
        }
        return Optional.empty();
    }

    @Override
    public synchronized  <T extends Command<?>> Iterator<EntityHandle<T>> commandIterator(Class<T> klass) {
        return commands.values().stream().
                filter(command -> klass.isAssignableFrom(command.getClass())).
                map(command -> new EntityHandle<T>(this, command.uuid())).
                iterator();
    }

    @Override
    public synchronized <T extends Event> Iterator<EntityHandle<T>> eventIterator(Class<T> klass) {
        return events.values().stream().
                filter(event -> klass.isAssignableFrom(event.getClass())).
                map(event -> new EntityHandle<T>(this, event.uuid())).
                iterator();
    }

    @Override
    public synchronized void clear() {
        events.clear();
        commands.clear();
        eventCommands.clear();
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> long size(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return Iterators.size(eventIterator((Class<Event>)klass));
        }
        if (Command.class.isAssignableFrom(klass)) {
            return Iterators.size(commandIterator((Class<Command<?>>)klass));
        }
        return 0;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity>  boolean isEmpty(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return !eventIterator((Class<Event>)klass).hasNext();
        }
        if (Command.class.isAssignableFrom(klass)) {
            return !commandIterator((Class<Command<?>>)klass).hasNext();
        }
        return true;
    }

    private static class EventConsumer implements Consumer<Event> {

        private final Command command;
        private final Journal.Listener listener;

        @Getter
        private Map<UUID, Event> events = new HashMap<>();
        @Getter
        private Map<UUID, UUID>  eventCommands = new HashMap<>();
        private final HybridTimestamp ts;

        public EventConsumer(Map<UUID, Event> events, Map<UUID, UUID> eventCommands, Command command, Journal.Listener listener) {
            this.events = events;
            this.eventCommands = eventCommands;
            this.command = command;
            this.listener = listener;
            ts = command.timestamp().clone();
        }

        @Override
        @SneakyThrows
        public synchronized void accept(Event event) {
            ts.update();
            event.timestamp(ts.clone());

            Layout<Event> layout = new Layout<>((Class<Event>)event.getClass());
            Serializer<Event> serializer = new Serializer<>(layout);
            Deserializer<Event> deserializer = new Deserializer<>(layout);

            ByteBuffer buffer = serializer.serialize(event);
            buffer.rewind();
            deserializer.deserialize(event, buffer);

            events.put(event.uuid(), event);
            eventCommands.put(event.uuid(), command.uuid());
            listener.onEvent(event);
        }
    }
}
