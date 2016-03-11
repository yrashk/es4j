/**
 * Copyright 2016 Eventchain team
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
package org.eventchain;

import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AbstractService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Memory-based {@link Journal} implementation. Not meant to be used in production.
 */
@Component
public class MemoryJournal extends AbstractService implements Journal {

    private Repository repository;

    @Override
    @Reference
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
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
    public synchronized long journal(Command<?> command, Journal.Listener listener, LockProvider lockProvider) {
        EventConsumer eventConsumer = new EventConsumer(command, listener);
        commands.put(command.uuid(), command);
        Stream<Event> events = command.events(repository, lockProvider);
        long count = events.peek(eventConsumer).count();
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

    private class EventConsumer implements Consumer<Event> {

        private final Command command;
        private final Journal.Listener listener;

        public EventConsumer(Command command, Journal.Listener listener) {
            this.command = command;
            this.listener = listener;
        }

        @Override
        public void accept(Event event) {
            events.put(event.uuid(), event);
            eventCommands.put(event.uuid(), command.uuid());
            listener.onEvent(event);
        }
    }
}
