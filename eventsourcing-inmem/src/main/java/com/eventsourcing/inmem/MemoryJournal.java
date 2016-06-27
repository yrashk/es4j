/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.inmem;

import com.eventsourcing.*;
import com.eventsourcing.events.CommandTerminatedExceptionally;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.ObjectDeserializer;
import com.eventsourcing.layout.ObjectSerializer;
import com.eventsourcing.layout.Serialization;
import com.eventsourcing.layout.binary.BinarySerialization;
import com.eventsourcing.layout.binary.ObjectBinaryDeserializer;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.binary.ObjectBinarySerializer;
import com.eventsourcing.repository.Journal;
import com.eventsourcing.repository.JournalEntityHandle;
import com.eventsourcing.repository.LockProvider;
import com.eventsourcing.utils.CloseableWrappingIterator;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AbstractService;
import com.googlecode.cqengine.index.support.CloseableIterator;
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

    private static final Serialization serialization = BinarySerialization.getInstance();

    private Repository repository;

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    private Map<UUID, Command> commands = new HashMap<>();
    private Map<UUID, Event> events = new HashMap<>();

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
    public synchronized long journal(Command<?, ?> command, Journal.Listener listener, LockProvider lockProvider)
            throws Exception {
        Map<UUID, Event> events_ = new HashMap<>();
        EventConsumer eventConsumer = new EventConsumer(events_, command, listener);

        Stream<? extends Event> events;
        Exception exception = null;

        try {
            EventStream<?> eventStream = command.events(repository, lockProvider);
            listener.onCommandStateReceived(eventStream.getState());
            events = eventStream.getStream();
        } catch (Exception e) {
            events = Stream.of((Event) new CommandTerminatedExceptionally(command.uuid(), e));
            exception = e;
        }

        long count = 0;

        try {
            count = events.peek(new Consumer<Event>() {
                @Override public void accept(Event event) {
                    eventConsumer.accept(event);
                    eventConsumer.accept(new EventCausalityEstablished().event(event.uuid()).command(command.uuid()));
                }
            }).count();
            events_ = eventConsumer.getEvents();
        } catch (Exception e) {
            events_.clear();
            listener.onAbort(e);
            exception = e;
            try {
                count = Stream.of((Event)new CommandTerminatedExceptionally(command.uuid(), e)).peek(eventConsumer)
                              .count();
            } catch (Exception e1) {
                events_.clear();
                exception = e1;
            }
        }

        this.events.putAll(events_);

        ObjectSerializer<Command> serializer = serialization.getSerializer(command.getClass());
        ObjectDeserializer<Command> deserializer = serialization.getDeserializer(command.getClass());

        ByteBuffer buffer = serializer.serialize(command);
        buffer.rewind();
        deserializer.deserialize(command, buffer);

        commands.put(command.uuid(), command);

        listener.onCommit();

        if (exception != null) {
            throw exception;
        }

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
    public synchronized <T extends Command<?, ?>> CloseableIterator<EntityHandle<T>> commandIterator(Class<T> klass) {
        return new CloseableWrappingIterator<>(commands.values().stream()
                                                       .filter(command -> klass.isAssignableFrom(command.getClass()))
                                                       .map(command -> (EntityHandle<T>) new JournalEntityHandle<T>(this, command.uuid())).iterator());
    }

    @Override
    public synchronized <T extends Event> CloseableIterator<EntityHandle<T>> eventIterator(Class<T> klass) {
        return new CloseableWrappingIterator<>(events.values().stream()
                 .filter(event -> klass.isAssignableFrom(event.getClass()))
                 .map(event -> (EntityHandle<T>) new JournalEntityHandle<T>(this, event.uuid())).iterator());
    }

    @Override
    public synchronized void clear() {
        events.clear();
        commands.clear();
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> long size(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return Iterators.size(eventIterator((Class<Event>) klass));
        }
        if (Command.class.isAssignableFrom(klass)) {
            return Iterators.size(commandIterator((Class<Command<?, ?>>) klass));
        }
        return 0;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> boolean isEmpty(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return !eventIterator((Class<Event>) klass).hasNext();
        }
        if (Command.class.isAssignableFrom(klass)) {
            return !commandIterator((Class<Command<?, ?>>) klass).hasNext();
        }
        return true;
    }

    private static class EventConsumer implements Consumer<Event> {

        private final Command command;
        private final Journal.Listener listener;

        @Getter
        private Map<UUID, Event> events = new HashMap<>();
        private final HybridTimestamp ts;

        public EventConsumer(Map<UUID, Event> events, Command command,
                             Journal.Listener listener) {
            this.events = events;
            this.command = command;
            this.listener = listener;
            ts = command.timestamp().clone();
        }

        @Override
        @SneakyThrows
        public synchronized void accept(Event event) {
            if (event.timestamp() == null) {
                ts.update();
                event.timestamp(ts.clone());
            } else {
                ts.update(event.timestamp().clone());
            }

            ObjectSerializer<Event> serializer = serialization.getSerializer(event.getClass());
            ObjectDeserializer<Event> deserializer = serialization.getDeserializer(event.getClass());

            ByteBuffer buffer = serializer.serialize(event);
            buffer.rewind();
            deserializer.deserialize(event, buffer);

            events.put(event.uuid(), event);
            listener.onEvent(event);
        }
    }
}
