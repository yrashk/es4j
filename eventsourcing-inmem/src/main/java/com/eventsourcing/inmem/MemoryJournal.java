/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.inmem;

import com.eventsourcing.*;
import com.eventsourcing.layout.ObjectDeserializer;
import com.eventsourcing.layout.ObjectSerializer;
import com.eventsourcing.layout.Serialization;
import com.eventsourcing.layout.binary.BinarySerialization;
import com.eventsourcing.repository.AbstractJournal;
import com.eventsourcing.repository.JournalEntityHandle;
import com.eventsourcing.utils.CloseableWrappingIterator;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AbstractService;
import com.googlecode.cqengine.index.support.CloseableIterator;
import lombok.Getter;
import lombok.Setter;
import org.osgi.service.component.annotations.Component;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory-based {@link Journal} implementation. Not meant to be used in production.
 */
@Component(property = {"type=MemoryJournal"}, service = Journal.class)
public class MemoryJournal extends AbstractService implements Journal, AbstractJournal {

    private static final Serialization serialization = BinarySerialization.getInstance();

    @Getter @Setter
    private Repository repository;

    private Map<UUID, Command> commands = new ConcurrentHashMap<>();
    private Map<UUID, Event> events = new ConcurrentHashMap<>();

    private EntityLayoutExtractor entityLayoutExtractor = new EntityLayoutExtractor(this);

    @Override
    protected void doStart() {
        if (repository == null) {
            notifyFailed(new IllegalStateException("repository == null"));
        }
        notifyStarted();
    }

    @Override public void onCommandsAdded(Set<Class<? extends Command>> commands) {
        commands.forEach(entityLayoutExtractor);
    }

    @Override public void onEventsAdded(Set<Class<? extends Event>> events) {
        events.forEach(entityLayoutExtractor);
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }

    static class Transaction implements AbstractJournal.Transaction {
        private final Map<UUID, Event> events = new HashMap<>();
        @Setter
        private Command command;
        private final MemoryJournal journal;

        Transaction(MemoryJournal journal) {this.journal = journal;}

        @Override public void rollback() {
        }

        @Override public void commit() {
            journal.events.putAll(events);
            journal.commands.put(command.uuid(), command);
        }
    }

    @Override public void record(AbstractJournal.Transaction tx, Command<?, ?> command) {
        ObjectSerializer<Command> serializer = serialization.getSerializer(command.getClass());
        ObjectDeserializer<Command> deserializer = serialization.getDeserializer(command.getClass());

        ByteBuffer buffer = serializer.serialize(command);
        buffer.rewind();
        Command command1 = deserializer.deserialize(buffer);
        command1.uuid(command.uuid());

        ((Transaction) tx).setCommand(command1);
    }

    @Override public void record(AbstractJournal.Transaction tx, Event event) {
        ObjectSerializer<Event> serializer = serialization.getSerializer(event.getClass());
        ObjectDeserializer<Event> deserializer = serialization.getDeserializer(event.getClass());

        ByteBuffer buffer = serializer.serialize(event);
        buffer.rewind();
        Event event1 = deserializer.deserialize(buffer);
        event1.uuid(event.uuid());

        ((Transaction) tx).events.put(event1.uuid(), event1);
    }

    @Override public AbstractJournal.Transaction beginTransaction() {
        return new Transaction(this);
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
                                                       .map(command -> (EntityHandle<T>) new JournalEntityHandle<T>(
                                                               this, command.uuid())).iterator());
    }

    @Override
    public synchronized <T extends Event> CloseableIterator<EntityHandle<T>> eventIterator(Class<T> klass) {
        return new CloseableWrappingIterator<>(events.values().stream()
                                                     .filter(event -> klass.isAssignableFrom(event.getClass()))
                                                     .map(event -> (EntityHandle<T>) new JournalEntityHandle<T>(this,
                                                                                                                event.uuid()))
                                                     .iterator());
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

}
