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
package org.eventchain.h2;

import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.AbstractService;
import lombok.*;
import org.eventchain.*;
import org.eventchain.hlc.HybridTimestamp;
import org.eventchain.layout.Deserializer;
import org.eventchain.layout.Layout;
import org.eventchain.layout.Serializer;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Component(properties = "journal.properties")
public class MVStoreJournal extends AbstractService implements Journal {
    private Repository repository;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE) // getter and setter for tests
    private MVStore store;
    private MVMap<UUID, byte[]> commandPayloads;
    private MVMap<UUID, Long> commandTimestamps;
    private MVMap<UUID, byte[]> commandHashes;
    private MVMap<byte[], Boolean> hashCommands;
    private MVMap<UUID, byte[]> eventPayloads;
    private MVMap<UUID, UUID> eventCommands;
    private MVMap<UUID, Long> eventTimestamps;
    private MVMap<byte[], Boolean> hashEvents;
    private MVMap<UUID, byte[]> eventHashes;
    private MVMap<byte[], byte[]> commandEvents;


    public MVStoreJournal(MVStore store) {
        this.store = store;
    }

    public MVStoreJournal() {
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        store = MVStore.open((String) ctx.getProperties().get("filename"));
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        store.close();
    }

    @Override
    protected void doStart() {
        if (repository == null) {
            notifyFailed(new IllegalStateException("repository == null"));
        }

        if (store == null) {
            notifyFailed(new IllegalStateException("store == null"));
        }

        initializeStore();

        repository.getCommands().forEach(new EntityLayoutExtractor());
        repository.getEvents().forEach(new EntityLayoutExtractor());

        notifyStarted();
    }

    @Override
    public void onCommandsAdded(Set<Class<? extends Command>> commands) {
        commands.forEach(new EntityLayoutExtractor());
    }

    @Override
    public void onEventsAdded(Set<Class<? extends Event>> events) {
        events.forEach(new EntityLayoutExtractor());
    }

    void initializeStore() {
        store.setAutoCommitDelay(0);
        MVMap<String, Object> info = store.openMap("info");
        info.putIfAbsent("version", 1);
        store.commit();

        commandPayloads = store.openMap("commandPayloads");
        commandTimestamps = store.openMap("commandTimestamps");
        commandHashes = store.openMap("commandPayloads");
        hashCommands = store.openMap("hashCommands");
        eventPayloads = store.openMap("eventPayloads");
        eventCommands = store.openMap("eventCommands");
        eventTimestamps = store.openMap("eventTimestamps");
        eventHashes = store.openMap("eventHashes");
        hashEvents = store.openMap("hashEvents");
        commandEvents = store.openMap("commandEvents");
    }

    @Override
    protected void doStop() {
        store.close();
        notifyStopped();
    }

    private Map<String, Layout> layoutsByHash = new HashMap<>();
    private Map<String, Layout> layoutsByClass = new HashMap<>();

    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
    }

    @Override @SuppressWarnings("unchecked")
    @Synchronized("store")
    public long journal(Command<?> command, Journal.Listener listener, LockProvider lockProvider) throws Exception {
        long version = store.getCurrentVersion();
        try {

            Layout commandLayout = layoutsByClass.get(command.getClass().getName());

            ByteBuffer buffer = new Serializer<>(commandLayout).serialize(command);

            ByteBuffer hashBuffer = ByteBuffer.allocate(16 + commandLayout.getHash().length);
            hashBuffer.put(commandLayout.getHash());
            hashBuffer.putLong(command.uuid().getMostSignificantBits());
            hashBuffer.putLong(command.uuid().getLeastSignificantBits());

            HybridTimestamp ts = command.timestamp().clone();

            long count = command.events(repository).peek(new Consumer<Event>() {
                @Override
                @SneakyThrows
                public void accept(Event event) {
                    ts.update();
                    event.timestamp(ts.clone());

                    Layout layout = layoutsByClass.get(event.getClass().getName());

                    ByteBuffer buffer1 = new Serializer<>(layout).serialize(event);

                    ByteBuffer hashBuffer1 = ByteBuffer.allocate(16 + layout.getHash().length);
                    hashBuffer1.put(layout.getHash());
                    hashBuffer1.putLong(event.uuid().getMostSignificantBits());
                    hashBuffer1.putLong(event.uuid().getLeastSignificantBits());

                    ByteBuffer commandEventBuf = ByteBuffer.allocate(16 * 2);
                    commandEventBuf.putLong(command.uuid().getMostSignificantBits());
                    commandEventBuf.putLong(command.uuid().getLeastSignificantBits());
                    commandEventBuf.putLong(event.uuid().getMostSignificantBits());
                    commandEventBuf.putLong(event.uuid().getLeastSignificantBits());

                    eventPayloads.put(event.uuid(), buffer1.array());
                    hashEvents.put(hashBuffer1.array(), true);
                    eventHashes.put(event.uuid(), layout.getHash());
                    eventTimestamps.put(event.uuid(), event.timestamp().timestamp());
                    eventCommands.put(event.uuid(), command.uuid());
                    commandEvents.put(commandEventBuf.array(), layout.getHash());

                    listener.onEvent(event);
                }
            }).count();

            commandPayloads.put(command.uuid(), buffer.array());
            hashCommands.put(hashBuffer.array(), true);
            commandTimestamps.put(command.uuid(), command.timestamp().timestamp());
            commandHashes.put(command.uuid(), commandLayout.getHash());

            listener.onCommit();

            store.commit();
            return count;
        } catch (Exception e) {
            store.rollback();
            listener.onAbort(e);
            throw e;
        }

    }

    @Override
    @SneakyThrows @SuppressWarnings("unchecked")
    public <T extends Entity> Optional<T> get(UUID uuid) {
        if (commandPayloads.containsKey(uuid)) {
            byte[] payload = commandPayloads.get(uuid);
            Long timestamp = commandTimestamps.get(uuid);
            String encodedHash = BaseEncoding.base16().encode(commandHashes.get(uuid));
            Layout<Command<?>> layout = layoutsByHash.get(encodedHash);
            Command command = (Command) layout.getLayoutClass().newInstance().uuid(uuid).timestamp(new HybridTimestamp(null, timestamp));
            new Deserializer<>(layout).deserialize(command, ByteBuffer.wrap(payload));
            return Optional.of((T) command);
        }
        if (eventPayloads.containsKey(uuid)) {
            byte[] payload = eventPayloads.get(uuid);
            Long timestamp = eventTimestamps.get(uuid);
            String encodedHash = BaseEncoding.base16().encode(eventHashes.get(uuid));
            Layout<Event> layout = layoutsByHash.get(encodedHash);
            Event event = (Event) layout.getLayoutClass().newInstance().uuid(uuid).timestamp(new HybridTimestamp(null, timestamp));
            new Deserializer<>(layout).deserialize(event, ByteBuffer.wrap(payload));
            return Optional.of((T) event);
        }
        return Optional.empty();

    }

    @Override
    public <T extends Command<?>> Iterator<EntityHandle<T>> commandIterator(Class<T> klass) {
        Layout layout = layoutsByClass.get(klass.getName());
        byte[] hash = layout.getHash();
        Cursor<byte[], Boolean> cursor = hashCommands.cursor(hashCommands.ceilingKey(hash));
        return new CursorIterator<>(cursor, bytes -> Bytes.indexOf(bytes, hash) == 0, new EntityFunction<>(hash, layout));
    }

    @Override
    public <T extends Event> Iterator<EntityHandle<T>> eventIterator(Class<T> klass) {
        Layout layout = layoutsByClass.get(klass.getName());
        byte[] hash = layout.getHash();
        Cursor<byte[], Boolean> cursor = hashEvents.cursor(hashCommands.ceilingKey(hash));
        return new CursorIterator<>(cursor, bytes -> Bytes.indexOf(bytes, hash) == 0, new EntityFunction<>(hash, layout));
    }

    @Override
    public void clear() {
        commandPayloads.clear();
        commandTimestamps.clear();
        hashCommands.clear();
        eventPayloads.clear();
        eventCommands.clear();
        eventTimestamps.clear();
        eventHashes.clear();
        hashEvents.clear();
        commandEvents.clear();
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> long size(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return Iterators.size(eventIterator((Class<Event>)klass));
        }
        if (Command.class.isAssignableFrom(klass)) {
            return Iterators.size(commandIterator((Class<Command<?>>)klass));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public <T extends Entity> boolean isEmpty(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return !eventIterator((Class<Event>)klass).hasNext();
        }
        if (Command.class.isAssignableFrom(klass)) {
            return !commandIterator((Class<Command<?>>)klass).hasNext();
        }
        throw new IllegalArgumentException();
    }

    private class EntityLayoutExtractor implements Consumer<Class<? extends Entity>> {
        @Override
        @SneakyThrows
        public void accept(Class<? extends Entity> aClass) {
            Layout<? extends Entity> layout = new Layout<>(aClass);
            byte[] hash = layout.getHash();
            String encodedHash = BaseEncoding.base16().encode(hash);
            layoutsByHash.put(encodedHash, layout);
            layoutsByClass.put(aClass.getName(), layout);
        }

    }

    static private class CursorIterator<K, V, R> implements Iterator<R> {

        private final Cursor<K, V> cursor;
        private Function<K, Boolean> hasNext;
        private final BiFunction<K, V, R> function;
        private K next;

        public CursorIterator(Cursor<K, V> cursor, Function<K, Boolean> hasNext, BiFunction<K, V, R>function) {
            this.cursor = cursor;
            this.hasNext = hasNext;
            this.function = function;
        }

        @Override
        public boolean hasNext() {
            return cursor.hasNext() && hasNext.apply(next = cursor.next());
        }

        @Override
        public R next() {
            if (next == null) {
                if (!hasNext()) {
                    return null;
                }
            }
            R result = function.apply(next, cursor.getValue());
            next = null;
            return result;
        }
    }

    private class EntityFunction<T extends Entity> implements BiFunction<byte[], Boolean, EntityHandle<T>> {
        private final byte[] hash;
        private final Layout layout;

        public EntityFunction(byte[] hash, Layout layout) {
            this.hash = hash;
            this.layout = layout;
        }

        @Override
        public EntityHandle<T> apply(byte[] bytes, Boolean aBoolean) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            UUID uuid = new UUID(buffer.getLong(hash.length), buffer.getLong(hash.length + 8));
            return new EntityHandle<>(MVStoreJournal.this, uuid);
        }
    }

}
