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

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.cqengine.IndexedCollection;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eventchain.hlc.HybridTimestamp;
import org.eventchain.hlc.PhysicalTimeProvider;
import org.eventchain.index.IndexEngine;
import org.eventchain.layout.Deserializer;
import org.eventchain.layout.Layout;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class CommandConsumer<T extends Command<C>, C> extends AbstractService {


    private final Class<T> commandClass;
    private final PhysicalTimeProvider timeProvider;
    private final Repository repository;
    private final Journal journal;
    private final IndexEngine indexEngine;
    private final LockProvider lockProvider;
    private final Layout<T> layout;
    private final Deserializer<T> deserializer;

    private static class CommandEvent<T extends Command<?>, C> {
        T command;
        TrackingLockProvider lockProvider;
        CompletableFuture<C> completed;

        @SneakyThrows
        public CommandEvent(Class<T> commandClass) {
            command = commandClass.newInstance();
        }
    }

    public static final int RING_BUFFER_SIZE = 1024;
    private RingBuffer<CommandEvent<T, C>> ringBuffer;
    private Disruptor<CommandEvent<T, C>> disruptor;

    private HybridTimestamp timestamp;

    private static class JournalListener implements Journal.Listener {
        private final IndexEngine indexEngine;
        private final Journal journal;
        private final Command<?> command;

        private JournalListener(IndexEngine indexEngine, Journal journal, Command<?> command) {
            this.indexEngine = indexEngine;
            this.journal = journal;
            this.command = command;
        }

        @Override @SuppressWarnings("unchecked")
        public void onEvent(Event event) {
            IndexedCollection<EntityHandle<Event>> coll = indexEngine.getIndexedCollection((Class<Event>) event.getClass());
            coll.add(new EntityHandle<>(journal, event.uuid()));
        }

        @Override @SuppressWarnings("unchecked")
        public void onCommit() {
            IndexedCollection<EntityHandle<Command<?>>> coll = indexEngine.getIndexedCollection((Class<Command<?>>) command.getClass());
            coll.add(new EntityHandle<>(journal, command.uuid()));
        }
    }

    private static class TrackingLockProvider extends AbstractService implements LockProvider {

        private final Set<Lock> locks = new HashSet<>();
        private final LockProvider lockProvider;

        private TrackingLockProvider(LockProvider lockProvider) {
            this.lockProvider = lockProvider;
        }

        private void release() {
            for (Lock lock : locks) {
                lock.unlock();
            }
        }

        @Override
        public Lock lock(Object lock) {
            Lock l = lockProvider.lock(lock);
            locks.add(l);
            return new TrackingLock(l);
        }

        @Override
        protected void doStart() {
            notifyStarted();
        }

        @Override
        protected void doStop() {
            notifyStopped();
        }

        class TrackingLock implements Lock {

            private final Lock lock;

            public TrackingLock(Lock lock) {
                this.lock = lock;
            }

            @Override
            public void unlock() {
                lock.unlock();
                locks.remove(lock);
            }

            @Override
            public boolean isLocked() {
                return lock.isLocked();
            }
        }
    }

    @SneakyThrows
    public CommandConsumer(Class<T> commandClass, PhysicalTimeProvider timeProvider,
                           Repository repository, Journal journal, IndexEngine indexEngine, LockProvider lockProvider) {
        this.commandClass = commandClass;
        this.timeProvider = timeProvider;
        this.repository = repository;
        this.journal = journal;
        this.indexEngine = indexEngine;
        this.lockProvider = lockProvider;
        this.timestamp = new HybridTimestamp(timeProvider);
        layout = new Layout<>(commandClass);
        deserializer = new Deserializer<>(layout);
    }

    private void journal(CommandEvent<T, C> event, long sequence, boolean endOfBatch) throws Exception {
        timestamp.update();
        event.command.timestamp(timestamp);
        event.lockProvider = new TrackingLockProvider(this.lockProvider);
        event.lockProvider.startAsync().awaitRunning();
        journal.journal(event.command, new JournalListener(indexEngine, journal, event.command), event.lockProvider);
    }

    private void complete(CommandEvent<T, C> event, long sequence, boolean endOfBatch) throws Exception {
        if (!event.completed.isCompletedExceptionally()) {
            event.completed.complete(event.command.onCompletion());
            event.lockProvider.release();
        }
    }

    private void translate(CommandEvent<T, C> event, long sequence, T command, CompletableFuture<C> completed) {
        event.command = command;
        event.completed = completed;
    }

    private void translate(CommandEvent<T, C> event, long sequence, ByteBuffer buffer, CompletableFuture<C> completed) {
        deserializer.deserialize(event.command, buffer);
        event.completed = completed;
    }

    public CompletableFuture<C> publish(T command) {
        CompletableFuture<C> future = new CompletableFuture<>();
        ringBuffer.publishEvent(this::translate, command, future);
        return future;
    }

    public CompletableFuture<C> publish(ByteBuffer buffer) {
        CompletableFuture<C> future = new CompletableFuture<>();
        ringBuffer.publishEvent(this::translate,  buffer, future);
        return future;
    }


    @Override @SuppressWarnings("unchecked")
    protected void doStart() {
        log.info("Starting command consumer for {}", commandClass.getSimpleName());

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("eventchain-" + commandClass.getSimpleName() +"-%d").setDaemon(true).build();

        disruptor = new Disruptor<>((EventFactory<CommandEvent<T, C>>) () -> new CommandEvent(commandClass), RING_BUFFER_SIZE, threadFactory);
        disruptor.setDefaultExceptionHandler(new CommandEventExceptionHandler());

        List<EventHandler<CommandEvent>> eventHandlers =
                Arrays.asList(this::journal, this::complete);

        EventHandlerGroup<CommandEvent<T, C>> handler = disruptor.handleEventsWith(eventHandlers.get(0));

        for (EventHandler<CommandEvent> h : eventHandlers.subList(1, eventHandlers.size())) {
            handler = handler.then(h);
        }

        ringBuffer = disruptor.start();

        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }


    private class CommandEventExceptionHandler implements com.lmax.disruptor.ExceptionHandler<CommandEvent<T, C>> {

        @Override
        public void handleEventException(Throwable ex, long sequence, CommandEvent<T,C> event) {
            event.lockProvider.release();
            event.completed.completeExceptionally(ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {

        }
    }
}
