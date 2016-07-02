/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2;

import com.eventsourcing.*;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.ObjectDeserializer;
import com.eventsourcing.layout.ObjectSerializer;
import com.eventsourcing.layout.Serialization;
import com.eventsourcing.layout.binary.BinarySerialization;
import com.eventsourcing.repository.AbstractJournal;
import com.eventsourcing.repository.JournalEntityHandle;
import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.AbstractService;
import com.googlecode.cqengine.index.support.CloseableIterator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.db.TransactionStore;
import org.h2.mvstore.db.TransactionStore.TransactionMap;
import org.h2.mvstore.type.ObjectDataType;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(
        service = Journal.class,
        property = {"filename=journal.db", "type=MVStoreJournal", "jmx.objectname=com.eventsourcing:type=journal,name=MVStoreJournal"})
@Slf4j
public class MVStoreJournal extends AbstractService implements Journal, AbstractJournal {
    @Getter @Setter
    private Repository repository;

    private final EntityLayoutExtractor entityLayoutExtractor = new EntityLayoutExtractor(this);

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE) // getter and setter for tests
    private MVStore store;
    private TransactionMap<UUID, ByteBuffer> commandPayloads;
    private TransactionMap<UUID, byte[]> commandHashes;
    private TransactionMap<byte[], Boolean> hashCommands;
    private TransactionMap<UUID, ByteBuffer> eventPayloads;
    private TransactionMap<byte[], Boolean> hashEvents;
    private TransactionMap<UUID, byte[]> eventHashes;

    private MVMap<byte[], byte[]> layouts;
    private TransactionStore transactionStore;
    TransactionStore.Transaction readTx;

    public MVStoreJournal(MVStore store) {
        this();
        this.store = store;
    }

    private final static Serialization serialization = BinarySerialization.getInstance();

    @SneakyThrows
    public MVStoreJournal() {
        layoutInformationLayout = Layout.forClass(LayoutInformation.class);
        layoutInformationSerializer = serialization.getSerializer(LayoutInformation.class);
        layoutInformationDeserializer = serialization.getDeserializer(LayoutInformation.class);
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

        notifyStarted();
    }

    @Override
    public void onCommandsAdded(Set<Class<? extends Command>> commands) {
        commands.forEach(entityLayoutExtractor);
        entityLayoutExtractor.flush();
    }

    @Override
    public void onEventsAdded(Set<Class<? extends Event>> events) {
        events.forEach(entityLayoutExtractor);
        entityLayoutExtractor.flush();
    }


    void initializeStore() {
        MVMap<String, Object> info = store.openMap("info");
        info.putIfAbsent("version", 1);
        store.commit();

        transactionStore = new TransactionStore(this.store);
        transactionStore.init();

        readTx = transactionStore.begin();
        commandPayloads = readTx.openMap("commandPayloads", new ObjectDataType(), new ByteBufferDataType());
        commandHashes = readTx.openMap("commandHashes");
        hashCommands = readTx.openMap("hashCommands");
        eventPayloads = readTx.openMap("eventPayloads", new ObjectDataType(), new ByteBufferDataType());
        eventHashes = readTx.openMap("eventHashes");
        hashEvents = readTx.openMap("hashEvents");

        layouts = store.openMap("layouts");
    }

    @Override
    protected void doStop() {
        transactionStore.close();
        store.close();
        notifyStopped();
    }

    private Map<String, Layout> layoutsByHash = new HashMap<>();
    private Map<String, Layout> layoutsByClass = new HashMap<>();
    
    private Layout getLayout(Class<? extends Entity> klass) {
        Layout layout = layoutsByClass.computeIfAbsent(klass.getName(), new LayoutFunction<>(klass));
        if (getLayout(layout.getHash()) == null) {
            String encoded = BaseEncoding.base16().encode(layout.getHash());
            layoutsByHash.put(encoded, layout);
        }
        return layout;
    }

    private Layout getLayout(byte[] hash) {
        String encoded = BaseEncoding.base16().encode(hash);
        return layoutsByHash.get(encoded);
    }

    static class Transaction implements AbstractJournal.Transaction {
        @Getter
        final TransactionStore.Transaction tx;

        private final TransactionMap<UUID, byte[]> txEventHashes;
        private final TransactionMap<byte[], Boolean> txHashEvents;
        private final TransactionMap<UUID, ByteBuffer> txEventPayloads;

        Transaction(TransactionStore.Transaction tx) {
            this.tx = tx;
            txEventPayloads = tx.openMap("eventPayloads", new ObjectDataType(), new ByteBufferDataType());
            txHashEvents = tx.openMap("hashEvents");
            txEventHashes = tx.openMap("eventHashes");
        }

        @Override public void commit() {
            tx.prepare();
            tx.commit();
        }

        @Override public void rollback() {
            tx.rollback();
        }
    }

    @Override public AbstractJournal.Transaction beginTransaction() {
        return new Transaction(transactionStore.begin());
    }

    @Override public void record(AbstractJournal.Transaction tx, Command<?, ?> command) {
        TransactionStore.Transaction tx0 = ((Transaction) tx).getTx();
        TransactionMap<UUID, ByteBuffer> txCommandPayloads = tx0.openMap("commandPayloads", new ObjectDataType(),
                                                                        new ByteBufferDataType());
        TransactionMap<byte[], Boolean> txHashCommands = tx0.openMap("hashCommands");
        TransactionMap<UUID, byte[]> txCommandHashes = tx0.openMap("commandHashes");

        Layout commandLayout = getLayout(command.getClass());

        ByteBuffer hashBuffer = ByteBuffer.allocate(16 + 20); // based on SHA-1
        hashBuffer.put(commandLayout.getHash());
        hashBuffer.putLong(command.uuid().getMostSignificantBits());
        hashBuffer.putLong(command.uuid().getLeastSignificantBits());


        ByteBuffer buffer = serialization.getSerializer(command.getClass()).serialize(command);
        buffer.rewind();
        txCommandPayloads.tryPut(command.uuid(), buffer);
        txHashCommands.tryPut(hashBuffer.array(), true);
        txCommandHashes.tryPut(command.uuid(), commandLayout.getHash());
    }

    @SneakyThrows
    @Override public void record(AbstractJournal.Transaction tx, Event event) {
        Transaction tx0 = ((Transaction) tx);
        Layout layout = getLayout(event.getClass());

        ObjectSerializer serializer = serialization.getSerializer(event.getClass());
        int size = serializer.size(event);

        ByteBuffer payloadBuffer = ByteBuffer.allocate(size);
        serializer.serialize(event, payloadBuffer);
        payloadBuffer.rewind();

        tx0.txEventPayloads.tryPut(event.uuid(), payloadBuffer);

        ByteBuffer hashBuffer = ByteBuffer.allocate(20 + 16); // Based on SHA-1

        hashBuffer.rewind();
        hashBuffer.put(layout.getHash());
        hashBuffer.putLong(event.uuid().getMostSignificantBits());
        hashBuffer.putLong(event.uuid().getLeastSignificantBits());


        tx0.txHashEvents.tryPut(hashBuffer.array(), true);
        tx0.txEventHashes.tryPut(event.uuid(), layout.getHash());
    }



    @Override
    @SneakyThrows @SuppressWarnings("unchecked")
    public <T extends Entity> Optional<T> get(UUID uuid) {
        if (commandPayloads.containsKey(uuid)) {
            ByteBuffer payload = commandPayloads.get(uuid);
            payload.rewind();
            byte[] bytes = commandHashes.get(uuid);
            Layout<Command<?, ?>> layout = getLayout(bytes);
            Command command = (Command) serialization.getDeserializer(layout.getLayoutClass()).deserialize(payload);
            command.uuid(uuid);
            return Optional.of((T) command);
        }
        if (eventPayloads.containsKey(uuid)) {
            ByteBuffer payload = eventPayloads.get(uuid);
            payload.rewind();
            byte[] bytes = eventHashes.get(uuid);
            Layout<Event> layout = getLayout(bytes);
            Event event = (Event) serialization.getDeserializer(layout.getLayoutClass()).deserialize(payload);
            event.uuid(uuid);
            return Optional.of((T) event);
        }
        return Optional.empty();

    }

    @Override
    public <T extends Command<?, ?>> CloseableIterator<EntityHandle<T>> commandIterator(Class<T> klass) {
        return entityIterator(klass, hashCommands);
    }

    @Override
    public <T extends Event> CloseableIterator<EntityHandle<T>> eventIterator(Class<T> klass) {
        return entityIterator(klass, hashEvents);
    }

    @SneakyThrows
    private <T extends Entity> CloseableIterator<EntityHandle<T>> entityIterator(Class<T> klass,  TransactionMap map) {
        Layout layout = getLayout(klass);
        if (layout == null) {
            layout = Layout.forClass(klass);
            layoutsByClass.put(klass.getName(), layout);
        }
        byte[] hash = layout.getHash();
        Iterator<Map.Entry<byte[], Boolean>> iterator = map.entryIterator(map.higherKey(hash));
        return new EntityHandleIterator<>(iterator, bytes -> Bytes.indexOf(bytes, hash) == 0,
                                          new EntityFunction<>(hash));
    }

    @Override
    public void clear() {
        commandPayloads.clear();
        hashCommands.clear();
        eventPayloads.clear();
        eventHashes.clear();
        hashEvents.clear();
        layouts.clear();
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> long size(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return Iterators.size(eventIterator((Class<Event>) klass));
        }
        if (Command.class.isAssignableFrom(klass)) {
            return Iterators.size(commandIterator((Class<Command<?, ?>>) klass));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public <T extends Entity> boolean isEmpty(Class<T> klass) {
        if (Event.class.isAssignableFrom(klass)) {
            return !eventIterator((Class<Event>) klass).hasNext();
        }
        if (Command.class.isAssignableFrom(klass)) {
            return !commandIterator((Class<Command<?, ?>>) klass).hasNext();
        }
        throw new IllegalArgumentException();
    }

    @Accessors(fluent = true)
    public static class PropertyInformation {
        @Getter
        private final String name;

        @Getter
        private final String type;

        public PropertyInformation(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    @Accessors(fluent = true)
    public static class LayoutInformation {
        @Getter
        private final byte[] hash;

        @Getter
        private final String className;
        @Getter
        private final List<PropertyInformation> properties;

        public LayoutInformation(byte[] hash, String className,
                                 List<PropertyInformation> properties) {
            this.hash = hash;
            this.className = className;
            this.properties = properties;
        }
    }

    private final Layout<LayoutInformation> layoutInformationLayout;
    private final ObjectSerializer<LayoutInformation> layoutInformationSerializer;
    private final ObjectDeserializer<LayoutInformation> layoutInformationDeserializer;

    private static class LayoutFunction<X> implements Function<X, Layout> {
        private final Class<? extends Entity> klass;

        public LayoutFunction(Class<? extends Entity> klass) {this.klass = klass;}

        @SneakyThrows
        @Override public Layout apply(X n) {return Layout.forClass(klass);}
    }


    private class EntityLayoutExtractor extends AbstractJournal.EntityLayoutExtractor {

        private Queue<Class<? extends Entity>> queue = new LinkedTransferQueue<>();

        public EntityLayoutExtractor(AbstractJournal journal) {
            super(journal);
        }

        @Override
        @SneakyThrows
        public void accept(Class<? extends Entity> aClass) {
            Layout<? extends Entity> layout = Layout.forClass(aClass);
            byte[] hash = layout.getHash();
            String encodedHash = BaseEncoding.base16().encode(hash);
            layoutsByHash.put(encodedHash, layout);
            layoutsByClass.put(aClass.getName(), layout);
            List<PropertyInformation> properties = layout.getProperties().stream()
                    .map(property -> new PropertyInformation(property.getName(), property.getType()
                                                                                        .getBriefDescription()))
                     .collect(Collectors.toList());

            LayoutInformation layoutInformation = new LayoutInformation(hash, aClass.getName(), properties);
            layouts.put(hash, layoutInformationSerializer.serialize(layoutInformation).array());
            queue.add(aClass);
        }

        void flush() {
            queue.iterator().forEachRemaining(super::accept);
            queue.clear();
        }

    }

    static private class EntityHandleIterator<K, V, R> implements CloseableIterator<R> {

        private final Iterator<Map.Entry<K, V>> iterator;
        private Function<K, Boolean> hasNext;
        private final BiFunction<K, V, R> function;
        private Map.Entry<K, V> entry;

        public EntityHandleIterator(Iterator<Map.Entry<K, V>> iterator, Function<K, Boolean> hasNext,
                                    BiFunction<K, V, R> function) {
            this.iterator = iterator;
            this.hasNext = hasNext;
            this.function = function;
        }

        @Override
        public boolean hasNext() {
            if (iterator.hasNext()) {
                entry = iterator.next();
                return hasNext.apply(entry.getKey());
            } else {
                return false;
            }
        }

        @Override
        public R next() {
            if (entry == null) {
                entry = iterator.next();
            }
            R result = function.apply(entry.getKey(), entry.getValue());
            entry = null;
            return result;
        }

        @Override
        public void close() {

        }
    }

    private class EntityFunction<T extends Entity, V> implements BiFunction<byte[], V, EntityHandle<T>> {
        private final byte[] hash;

        public EntityFunction(byte[] hash) {
            this.hash = hash;
        }

        @Override
        public EntityHandle<T> apply(byte[] bytes, V value) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            UUID uuid = new UUID(buffer.getLong(hash.length), buffer.getLong(hash.length + 8));
            return new JournalEntityHandle<>(MVStoreJournal.this, uuid);
        }
    }
}
