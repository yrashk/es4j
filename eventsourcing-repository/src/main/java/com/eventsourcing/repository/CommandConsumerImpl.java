/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.*;
import com.eventsourcing.cep.events.DescriptionChanged;
import com.eventsourcing.events.CommandTerminatedExceptionally;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.events.JavaExceptionOccurred;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.PhysicalTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.google.common.util.concurrent.AbstractService;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
class CommandConsumerImpl extends AbstractService implements CommandConsumer {

    private Executor threadPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

    private final Repository repository;
    private final Journal journal;
    private final IndexEngine indexEngine;
    private final LockProvider lockProvider;

    @Getter
    private HybridTimestamp timestamp;


    @SneakyThrows
    public CommandConsumerImpl(Iterable<Class<? extends Command>> commandClasses,
                               PhysicalTimeProvider timeProvider,
                               Repository repository, Journal journal, IndexEngine indexEngine,
                               LockProvider lockProvider) {
        this.repository = repository;
        this.journal = journal;
        this.indexEngine = indexEngine;
        this.lockProvider = lockProvider;
        this.timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
    }

    private void timestamp(Entity entity, HybridTimestamp timestamp) {
        if (entity.timestamp() == null) {
            timestamp.update();
            entity.timestamp(timestamp.clone());
        } else {
            timestamp.update(timestamp.clone());
        }

    }

    private <S> EventStream<S> exceptionalTerminationStream(Exception e) {
        CommandTerminatedExceptionally commandTerminatedExceptionally = new CommandTerminatedExceptionally();
        return EventStream.of(Stream.of( new CommandTerminatedExceptionally(),
                                                DescriptionChanged.builder()
                                                                  .description(e.getMessage())
                                                                  .reference(commandTerminatedExceptionally.uuid()).build(),
                                                new JavaExceptionOccurred(e)));
    }

    private void onEvent(Event event,
                         Map<Class<? extends Event>, IndexedCollection<EntityHandle<Event>>> txCollections,
                         Map<EntitySubscriber, Set<UUID>> subscriptions,
                         Collection<EntitySubscriber> subscribers
                         ) {
        IndexedCollection<EntityHandle<Event>> coll = txCollections
                .computeIfAbsent(event.getClass(), klass -> new ConcurrentIndexedCollection<>());
        coll.add(new ResolvedEntityHandle<>(event));
        subscribers.stream()
                      .filter(s -> s.matches(repository, event))
                      .forEach(s -> subscriptions.get(s).add(event.uuid()));
    }


    @Override
    public <T, S, C extends Command<S, T>> CompletableFuture<T> publish(C command, Collection<EntitySubscriber>
            subscribers) {
        Map<EntitySubscriber, Set<UUID>> subscriptions = new HashMap<>();
        subscribers.forEach(s -> subscriptions.put(s, new HashSet<>()));

        Map<Class<? extends Event>, IndexedCollection<EntityHandle<Event>>> txCollections = new HashMap<>();

        CompletableFuture<T> future = new CompletableFuture<>();
        HybridTimestamp txTimestamp;
        synchronized (timestamp) {
            timestamp(command, timestamp);
            txTimestamp = timestamp.clone();
        }
        final HybridTimestamp commandTimestamp = txTimestamp.clone();
        threadPool.execute(
                new CommandHandler<>(commandTimestamp, command, txCollections, subscriptions, subscribers,
                                     future,
                                     txTimestamp));

        return future;
    }

    @Override @SuppressWarnings("unchecked")
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }

    private class CommandHandler<S, T, C extends Command<S, T>> implements Runnable {
        private final HybridTimestamp commandTimestamp;
        private final C command;
        private final Map<Class<? extends Event>, IndexedCollection<EntityHandle<Event>>> txCollections;
        private final Map<EntitySubscriber, Set<UUID>> subscriptions;
        private final Collection<EntitySubscriber> subscribers;
        private final CompletableFuture<T> future;
        private final HybridTimestamp txTimestamp;

        public CommandHandler(HybridTimestamp commandTimestamp, C command,
                              Map<Class<? extends Event>, IndexedCollection<EntityHandle<Event>>> txCollections,
                              Map<EntitySubscriber, Set<UUID>> subscriptions, Collection<EntitySubscriber> subscribers,
                              CompletableFuture<T> future, HybridTimestamp txTimestamp) {
            this.commandTimestamp = commandTimestamp;
            this.command = command;
            this.txCollections = txCollections;
            this.subscriptions = subscriptions;
            this.subscribers = subscribers;
            this.future = future;
            this.txTimestamp = txTimestamp;
        }

        @Override public void run() {
            HybridTimestamp ts = commandTimestamp.clone();
            HybridTimestamp startingTxTimestamp = ts.clone();

            TrackingLockProvider lockProvider = new TrackingLockProvider(CommandConsumerImpl.this.lockProvider);
            lockProvider.startAsync().awaitRunning();
            EventStream<S> eventStream;
            Exception exception = null;

            try {
                eventStream = command.events(repository, lockProvider);
            } catch (Exception e) {
                eventStream = CommandConsumerImpl.this.exceptionalTerminationStream(e);
                exception = e;
            }

            boolean pending = true;

            main:
            while (pending) {
                Journal.Transaction tx = journal.beginTransaction();
                Stream<? extends Event> stream = eventStream.getStream();
                Iterator<? extends Event> iterator = stream.iterator();

                try {
                    while (iterator.hasNext()) {
                        Event event = iterator.next();
                        CommandConsumerImpl.this.timestamp(event, ts);
                        event = journal.journal(tx, event);
                        EventCausalityEstablished causalityEstablished = EventCausalityEstablished.builder()
                                                                                                  .event(event.uuid())
                                                                                                  .command(
                                                                                                          command.uuid())
                                                                                                  .build();
                        CommandConsumerImpl.this.timestamp(causalityEstablished, ts);
                        causalityEstablished = (EventCausalityEstablished) journal.journal(tx, causalityEstablished);
                        CommandConsumerImpl.this.onEvent(event, txCollections, subscriptions, subscribers);
                        CommandConsumerImpl.this
                                .onEvent(causalityEstablished, txCollections, subscriptions, subscribers);
                    }
                } catch (Exception e) {
                    txCollections.clear();
                    tx.rollback();
                    eventStream = CommandConsumerImpl.this.exceptionalTerminationStream(e);
                    ts = startingTxTimestamp;
                    exception = e;
                    continue main;
                }

                pending = false;

                Command<S, T> command_ = journal.journal(tx, command);
                tx.commit();

                for (Map.Entry<Class<? extends Event>, IndexedCollection<EntityHandle<Event>>> pair :
                        txCollections.entrySet()) {
                    IndexedCollection<EntityHandle<Event>> value = pair.getValue();
                    indexEngine.getIndexedCollection((Class<Event>) pair.getKey()).addAll(value);
                }
                IndexedCollection<EntityHandle<Command<S, T>>> coll = indexEngine
                        .getIndexedCollection((Class<Command<S, T>>) command_.getClass());
                EntityHandle<Command<?, ?>> commandHandle = new JournalEntityHandle<>(journal, command_.uuid());
                coll.add(new ResolvedEntityHandle<>(command_));
                subscriptions.entrySet().stream()
                             .forEach(entry -> entry.getKey()
                                                    .accept(repository, entry.getValue()
                                                                 .stream()
                                                                 .map(uuid -> new JournalEntityHandle<>(journal,
                                                                                                        uuid))));
                subscribers.stream()
                           .filter(s -> s.matches(repository, command_))
                           .forEach(s -> s.accept(repository, Stream.of(commandHandle)));

                synchronized (timestamp) {
                    timestamp.update(txTimestamp);
                }

                T result = command.result(eventStream.getState(), repository, lockProvider);
                lockProvider.release();

                if (exception == null) {
                    future.complete(result);
                } else {
                    future.completeExceptionally(exception);
                }

            }

        }
    }
}
