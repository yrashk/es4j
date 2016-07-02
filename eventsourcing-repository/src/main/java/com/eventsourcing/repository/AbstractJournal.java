/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.*;
import com.eventsourcing.events.CommandTerminatedExceptionally;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.migrations.events.EntityLayoutIntroduced;
import com.eventsourcing.repository.commands.IntroduceEntityLayout;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public interface AbstractJournal extends Journal {

    interface Transaction {
        void commit();
        void rollback();
    }
    default long journal(Command<?, ?> command, Journal.Listener listener, LockProvider lockProvider) throws Exception {
        return journal(command, listener, lockProvider, null);
    }

    default long journal(Command<?, ?> command, Journal.Listener listener, LockProvider lockProvider, Stream<? extends
            Event> events)
            throws Exception {
        Transaction tx = beginTransaction();
        try {

            Stream<? extends Event> actualEvents;

            if (events == null) {
                EventStream<?> eventStream = command.events(getRepository(), lockProvider);
                listener.onCommandStateReceived(eventStream.getState());
                actualEvents = eventStream.getStream();
            } else {
                actualEvents = events;
            }

            EventConsumer eventConsumer = createEventConsumer(this, tx, command, listener);
            long count = actualEvents.peek(new Consumer<Event>() {
                @Override public void accept(Event event) {
                    eventConsumer.accept(event);
                    eventConsumer.accept(EventCausalityEstablished.builder()
                                                                  .event(event.uuid())
                                                                  .command(command.uuid())
                                                                  .build());
                }
            }).count();

            record(tx, command);

            tx.commit();
            listener.onCommit();

            return count;
        } catch (Exception e) {
            tx.rollback();
            listener.onAbort(e);

            // if we are having an exception NOT when journalling CommandTerminatedExceptionally
            if (events == null) {
                journal(command, listener, lockProvider,
                        Stream.of(new CommandTerminatedExceptionally(command.uuid(), e)));
            }

            throw e;
        }

    }

    void record(Transaction tx, Command<?, ?> command);
    void record(Transaction tx, Event event);

    Transaction beginTransaction();

    default EventConsumer createEventConsumer(AbstractJournal journal,  AbstractJournal.Transaction tx, Command<?, ?>
            command, Journal.Listener listener) {
        return new EventConsumer(journal, tx, command, listener);
    }

    class EventConsumer implements Consumer<Event> {
        private final HybridTimestamp ts;
        private final AbstractJournal journal;
        private final Transaction tx;
        private final Journal.Listener listener;

        public EventConsumer(AbstractJournal journal,
                             AbstractJournal.Transaction tx, Command<?, ?> command, Journal.Listener listener) {
            this.journal = journal;
            this.tx = tx;
            this.listener = listener;
            this.ts = command.timestamp().clone();
        }

        @Override
        @SneakyThrows
        public void accept(Event event) {
            if (event.timestamp() == null) {
                ts.update();
                event.timestamp(ts.clone());
            } else {
                ts.update(event.timestamp().clone());
            }

            journal.record(tx, event);

            listener.onEvent(event);
        }
    }

    class EntityLayoutExtractor implements Consumer<Class<? extends Entity>> {

        private final AbstractJournal journal;

        public EntityLayoutExtractor(AbstractJournal journal) {this.journal = journal;}

        @Override
        @SneakyThrows
        public void accept(Class<? extends Entity> aClass) {
            Layout<? extends Entity> layout = Layout.forClass(aClass);
            byte[] fingerprint = layout.getHash();
            Query<EntityHandle<EntityLayoutIntroduced>> query = equal(EntityLayoutIntroduced.FINGERPRINT,
                                                                      fingerprint);
            try(ResultSet<EntityHandle<EntityLayoutIntroduced>> resultSet = journal.getRepository()
                                                                                   .query(EntityLayoutIntroduced.class,
                                                                                          query)) {
                if (resultSet.isEmpty()) {
                    journal.getRepository().publish(new IntroduceEntityLayout(fingerprint, Optional.of(layout))).get();
                }
            }
        }

    }

}
