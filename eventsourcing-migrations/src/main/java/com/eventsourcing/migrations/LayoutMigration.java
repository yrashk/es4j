/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.migrations;

import com.eventsourcing.*;
import com.eventsourcing.events.EventCausalityEstablished;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.migrations.events.EntityLayoutIntroduced;
import com.eventsourcing.migrations.events.EntityLayoutReplaced;
import com.eventsourcing.repository.LockProvider;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.SneakyThrows;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.eventsourcing.index.EntityQueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.equal;

public class LayoutMigration<A extends Event, B extends Event> {

    private Layout<A> oldLayout;
    private Layout<B> newLayout;
    private Function<A, B> transformation;

    private final Class<A> oldClass;
    private final Class<B> newClass;

    private final boolean includeLayout;

    @SneakyThrows
    public LayoutMigration(Class<A> oldClass, Class<B> newClass, Function<A, B> transformation, boolean includeLayout) {
        this.includeLayout = includeLayout;
        this.oldLayout = new Layout<>(oldClass);
        this.oldClass = oldClass;
        this.newLayout = new Layout<>(newClass);
        this.newClass = newClass;
        this.transformation = transformation;

    }

    public LayoutMigration(Class<A> oldClass, Class<B> newClass, Function<A, B> transformation) {
        this(oldClass, newClass, transformation, true);
    }


    public Stream<? extends Event> events(Repository repository, LockProvider lockProvider) throws Exception {
        Lock lock = lockProvider.lock(oldClass.getName());
        Stream<? extends Event> acc = Stream.empty();
        Optional<EntityLayoutIntroduced> oldLayoutIntroduciton = layoutIntroduction(repository, oldLayout);
        if (!oldLayoutIntroduciton.isPresent()) {
            acc = Stream.concat(acc, Stream.of(makeLayoutIntroduction(oldLayout)));
        }
        Optional<EntityLayoutIntroduced> newLayoutIntroduction = layoutIntroduction(repository, newLayout);
        UUID newLayoutIntroductionUUID;
        if (!newLayoutIntroduction.isPresent()) {
            EntityLayoutIntroduced introduction = makeLayoutIntroduction(newLayout);
            acc = Stream.concat(acc, Stream.of(introduction));
            newLayoutIntroductionUUID = introduction.uuid();
        } else {
            newLayoutIntroductionUUID = newLayoutIntroduction.get().uuid();
        }
        EntityLayoutReplaced replacement = new EntityLayoutReplaced().fingerprint(oldLayout.getHash())
                                                                     .replacement(newLayoutIntroductionUUID);
        acc = Stream.concat(acc, Stream.of(replacement));
        ResultSet<EntityHandle<A>> resultSet = repository.query(oldClass, all(oldClass));
        Iterator<EntityHandle<A>> iterator = resultSet.iterator();
        Stream<Event> stream = StreamSupport
                .stream(Spliterators.spliterator(iterator, resultSet.size(), Spliterator.IMMUTABLE), false)
                .flatMap(h -> {
                    B transformed = transformation.apply(h.get());
                    try (ResultSet<EntityHandle<EventCausalityEstablished>> causality = repository
                            .query(EventCausalityEstablished.class, equal(EventCausalityEstablished.EVENT, h.uuid()))) {
                        Stream<EntityHandle<EventCausalityEstablished>> causalityStream = StreamSupport
                                .stream(Spliterators.spliterator(causality.iterator(), causality.size(),
                                                                 Spliterator.IMMUTABLE), false);
                        Function<EntityHandle<EventCausalityEstablished>, Event> entityHandleFunction = handle ->
                                new EventCausalityEstablished()
                                        .event(transformed.uuid())
                                        .command(handle.get().command());
                        return Stream.concat(Stream.of(transformed), causalityStream.map(entityHandleFunction));
                    }
                });
        stream.onClose(resultSet::close);
        acc = Stream.concat(acc, stream);
        lock.unlock();
        return acc;
    }

    private Optional<EntityLayoutIntroduced> layoutIntroduction(Repository repository, Layout<?> layout) {
        try (ResultSet<EntityHandle<EntityLayoutIntroduced>> resultSet = repository
                .query(EntityLayoutIntroduced.class,
                       equal(EntityLayoutIntroduced.FINGERPRINT,
                             layout.getHash()))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(resultSet.uniqueResult().get());
            }
        }
    }

    private EntityLayoutIntroduced makeLayoutIntroduction(Layout<?> layout) {
        EntityLayoutIntroduced introduction = new EntityLayoutIntroduced()
                .fingerprint(layout.getHash());
        if (includeLayout) {
            introduction.layout(Optional.of(layout));
        }
        return introduction;
    }
}
