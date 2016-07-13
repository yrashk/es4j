/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.protocols;

import com.eventsourcing.EventStream;
import com.eventsourcing.Model;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.cep.events.DescriptionChanged;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.queries.ModelCollectionQuery;
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

public class DescriptionProtocolTest extends RepositoryUsingTest {

    public DescriptionProtocolTest() {
        super(DescriptionChanged.class.getPackage(), DescriptionProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class ChangeDescription extends StandardCommand<Void, String> {

        @Getter
        private final UUID id;
        @Getter
        private final String description;

        @LayoutConstructor
        public ChangeDescription(UUID id, String description) {
            this.id = id;
            this.description = description;
        }

        @Builder
        public ChangeDescription(HybridTimestamp timestamp, UUID id, String description) {
            super(timestamp);
            this.id = id;
            this.description = description;
        }

        @Override
        public EventStream<Void> events() throws Exception {
            return EventStream.of(DescriptionChanged.builder()
                    .reference(id)
                    .description(description)
                    .timestamp(timestamp()).build());
        }

        @Override
        public String result() {
            return description;
        }
    }

    public static class TestModel implements Model, DescriptionProtocol {

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
    public void changingDescription() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestModel model = new TestModel(repository, UUID.randomUUID());

        ChangeDescription changeDescription = new ChangeDescription(model.getId(), "Description #1");
        repository.publish(changeDescription).get();
        assertEquals(model.description(), "Description #1");

        ChangeDescription changeBefore = ChangeDescription.builder()
                                                          .id(model.getId()).description("Description #0")
                                                          .timestamp(timestamp).build();
        assertTrue(changeBefore.timestamp().compareTo(changeDescription.timestamp()) < 0);
        repository.publish(changeBefore).get();
        assertEquals(model.description(), "Description #1"); // earlier change shouldn't affect the description


        changeDescription = new ChangeDescription(model.getId(), "Description #2");
        repository.publish(changeDescription).get();
        assertEquals(model.description(), "Description #2");
    }

    @Test
    @SneakyThrows
    public void query() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        NameProtocolTest.TestModel model = new NameProtocolTest.TestModel(repository, UUID.randomUUID());

        ChangeDescription changeDescription = new ChangeDescription(model.getId(), "Description");
        repository.publish(changeDescription).get();

        Collection<TestModel> models = ModelCollectionQuery.query(repository, DescriptionProtocol.described("Description", TestModel::lookup));

        assertEquals(models.size(), 1);
        assertTrue(models.stream().anyMatch(m -> m.description().contentEquals("Description")));
    }
}