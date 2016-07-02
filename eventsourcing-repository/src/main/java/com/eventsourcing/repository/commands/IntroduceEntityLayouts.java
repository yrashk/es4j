/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository.commands;

import com.eventsourcing.*;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.migrations.events.EntityLayoutIntroduced;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.SneakyThrows;

import java.util.Base64;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public class IntroduceEntityLayouts extends StandardCommand<Lock, Void> {

    private Iterable<Class<? extends Entity>> entities;

    @LayoutConstructor
    public IntroduceEntityLayouts() {}

    public IntroduceEntityLayouts(Iterable<Class<? extends Entity>> entities) {this.entities = entities;}

    @Override public EventStream<Lock> events(Repository repository, LockProvider lockProvider) throws Exception {
        Lock lock = lockProvider.lock(getClass().getName());
        Stream<Class<? extends Entity>> stream = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(entities.iterator(), Spliterator.IMMUTABLE), false);
        return EventStream.ofWithState(lock, stream.flatMap(new EntityStreamFunction(repository)));
    }

    @Override public Void result(Lock lock) {
        lock.unlock();
        return null;
    }

    private static class EntityStreamFunction implements Function<Class<? extends Entity>, Stream<Event>> {
        private final Repository repository;

        public EntityStreamFunction(Repository repository) {this.repository = repository;}

        @SneakyThrows
        @Override public Stream<Event> apply(Class<? extends Entity> entity) {
        Layout<? extends Entity> layout = Layout.forClass(entity);
            byte[] fingerprint = layout.getHash();
            Query<EntityHandle<EntityLayoutIntroduced>> query = equal(EntityLayoutIntroduced.FINGERPRINT,
                                                                      Base64.getEncoder().encodeToString(fingerprint));
            try (ResultSet<EntityHandle<EntityLayoutIntroduced>> resultSet = repository
                    .query(EntityLayoutIntroduced.class,
                           query)) {
                if (resultSet.isEmpty()) {
                    return Stream.of(new EntityLayoutIntroduced(fingerprint, Optional.of(layout)));
                } else {
                    return Stream.empty();
                }
            }


        }
    }
}
