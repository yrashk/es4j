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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class DisruptorCommandConsumer extends AbstractService implements CommandConsumer {


    private final Iterable<Class<? extends Command>> commandClasses;
    private final PhysicalTimeProvider timeProvider;
    private final Repository repository;
    private final Journal journal;
    private final IndexEngine indexEngine;
    private final LockProvider lockProvider;
    private final Map<Class<? extends Command>, Layout> layouts = new HashMap<>();
    private final Map<Class<? extends Command>, Deserializer> deserializers = new HashMap<>();

    private static class CommandEvent {
        Map<Class<? extends Command>, Command> commands = new HashMap<>();
        TrackingLockProvider lockProvider;
        CompletableFuture completed;
        private Class<? extends Command> commandClass;

        @SneakyThrows
        public CommandEvent(Iterable<Class<? extends Command>> classes) {
            for (Class<? extends Command> cmd : classes) {
                commands.put(cmd, cmd.newInstance());
            }
        }

        public void setCommandClass(Class<Command> klass) {
            this.commandClass = klass;
        }
        public Command getCommand() {
            return commands.get(commandClass);
        }
    }

    public static final int RING_BUFFER_SIZE = 1024;
    private RingBuffer<CommandEvent> ringBuffer;
    private Disruptor<CommandEvent> disruptor;

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

        @Override
        public void onAbort(Throwable throwable) {

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
    public DisruptorCommandConsumer(Iterable<Class<? extends Command>> commandClasses, PhysicalTimeProvider timeProvider,
                                    Repository repository, Journal journal, IndexEngine indexEngine, LockProvider lockProvider) {
        this.commandClasses = commandClasses;
        this.timeProvider = timeProvider;
        this.repository = repository;
        this.journal = journal;
        this.indexEngine = indexEngine;
        this.lockProvider = lockProvider;
        this.timestamp = new HybridTimestamp(timeProvider);
        for (Class<? extends Command> cmd : commandClasses) {
            Layout<? extends Command> layout = new Layout<>(cmd);
            layouts.put(cmd, layout);
            deserializers.put(cmd, new Deserializer<>(layout));
        }
    }

    private void journal(CommandEvent event, long sequence, boolean endOfBatch) throws Exception {
        timestamp.update();
        Command command = event.getCommand();
        command.timestamp(timestamp);
        event.lockProvider = new TrackingLockProvider(this.lockProvider);
        event.lockProvider.startAsync().awaitRunning();
        journal.journal(command, new JournalListener(indexEngine, journal, command), event.lockProvider);
    }

    private void complete(CommandEvent event, long sequence, boolean endOfBatch) throws Exception {
        if (!event.completed.isCompletedExceptionally()) {
            event.completed.complete(event.getCommand().onCompletion());
            event.lockProvider.release();
        }
    }

    private <T, C extends Command<T>> void translate(CommandEvent event, long sequence, C command, CompletableFuture<T> completed) {
        event.setCommandClass((Class<Command>) command.getClass());
        event.commands.put(command.getClass(), command);
        event.completed = completed;
    }

    @Override
    public <T, C extends Command<T>> CompletableFuture<T> publish(C command) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ringBuffer.publishEvent(this::translate, command, future);
        return future;
    }


    @Override @SuppressWarnings("unchecked")
    protected void doStart() {
        log.info("Starting command consumer");

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("eventchain-%d").setDaemon(true).build();

        disruptor = new Disruptor<>(() -> new CommandEvent(commandClasses), RING_BUFFER_SIZE, threadFactory);
        disruptor.setDefaultExceptionHandler(new CommandEventExceptionHandler());

        List<EventHandler<CommandEvent>> eventHandlers =
                Arrays.asList(this::journal, this::complete);

        EventHandlerGroup<CommandEvent> handler = disruptor.handleEventsWith(eventHandlers.get(0));

        for (EventHandler<CommandEvent> h : eventHandlers.subList(1, eventHandlers.size())) {
            handler = handler.then(h);
        }

        ringBuffer = disruptor.start();

        notifyStarted();
    }

    @Override
    protected void doStop() {
        disruptor.shutdown();
        notifyStopped();
    }


    private class CommandEventExceptionHandler implements com.lmax.disruptor.ExceptionHandler<CommandEvent> {

        @Override
        public void handleEventException(Throwable ex, long sequence, CommandEvent event) {
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
