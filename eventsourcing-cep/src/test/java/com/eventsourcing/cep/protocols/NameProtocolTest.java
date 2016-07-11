/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.protocols;

import com.eventsourcing.*;
import com.eventsourcing.cep.events.NameChanged;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.Repository;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.queries.ModelCollectionQuery;
import com.eventsourcing.queries.ModelQueries;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class NameProtocolTest extends RepositoryUsingTest {

    public NameProtocolTest() {
        super(NameChanged.class.getPackage(), NameProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class Rename extends StandardCommand<Void, String> {

        @Getter
        private final UUID id;
        @Getter
        private final String name;

        @LayoutConstructor
        public Rename(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        @Builder
        public Rename(HybridTimestamp timestamp, UUID id, String name) {
            super(timestamp);
            this.id = id;
            this.name = name;
        }

        @Override
        public EventStream<Void> events() throws Exception {
            return EventStream.of(NameChanged.builder().reference(id).name(name).timestamp(timestamp()).build());
        }

        @Override
        public String result() {
            return name;
        }
    }

    public static class TestModel implements Model, NameProtocol {

        @Getter
        private final Repository repository;

        @Getter
        private final UUID id;

        public TestModel(Repository repository, UUID id) {
            this.repository = repository;
            this.id = id;
        }

        public static Optional<TestModel> lookup(Repository repository, UUID uuid) {
            return Optional.of(new TestModel(repository, uuid));
        }

    }

    @Test
    @SneakyThrows
    public void renaming() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestModel model = new TestModel(repository, UUID.randomUUID());

        Rename rename = new Rename(model.getId(), "Name #1");
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #1");

        Rename renameBefore = Rename.builder().id(model.getId()).name("Name #0").timestamp(timestamp).build();
        assertTrue(renameBefore.timestamp().compareTo(rename.timestamp()) < 0);
        repository.publish(renameBefore).get();
        assertEquals(model.name(), "Name #1"); // earlier change shouldn't affect the name


        rename = new Rename(model.getId(), "Name #2");
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #2");
    }

    @Test
    @SneakyThrows
    public void query() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestModel model = new TestModel(repository, UUID.randomUUID());

        Rename rename = new Rename(model.getId(), "Name");
        repository.publish(rename).get();

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, NameProtocol.named("Name", TestModel::lookup));

        assertEquals(models.size(), 1);
        assertTrue(models.stream().anyMatch(m -> m.name().contentEquals("Name")));
    }
}