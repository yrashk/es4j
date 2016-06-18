/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.protocols;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Model;
import com.eventsourcing.Repository;
import com.eventsourcing.cep.events.DescriptionChanged;
import com.eventsourcing.hlc.HybridTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DescriptionProtocolTest extends RepositoryTest {

    public DescriptionProtocolTest() {
        super(DescriptionChanged.class.getPackage(), DescriptionProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class ChangeDescription extends Command<String> {

        @Getter @Setter
        private UUID id;
        @Getter @Setter
        private String description;

        @Override
        public Stream<Event> events(Repository repository) throws Exception {
            return Stream.of((Event)new DescriptionChanged().reference(id).description(description).timestamp(timestamp
                                                                                                                   ()));
        }

        @Override
        public String onCompletion() {
            return description;
        }
    }

    @Accessors(fluent = true)
    public static class TestModel implements Model, DescriptionProtocol {

        @Getter @Accessors(fluent = false)
        private final Repository repository;

        @Getter
        private final UUID id;

        public TestModel(Repository repository, UUID id) {
            this.repository = repository;
            this.id = id;
        }

    }

    @Test
    @SneakyThrows
    public void changingDescription() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestModel model = new TestModel(repository, UUID.randomUUID());

        ChangeDescription changeDescription = new ChangeDescription().id(model.id()).description("Description #1");
        repository.publish(changeDescription).get();
        assertEquals(model.description(), "Description #1");

        ChangeDescription changeBefore = (ChangeDescription) new ChangeDescription().id(model.id()).description("Description #0")
                                                                                    .timestamp(timestamp);
        assertTrue(changeBefore.timestamp().compareTo(changeDescription.timestamp()) < 0);
        repository.publish(changeBefore).get();
        assertEquals(model.description(), "Description #1"); // earlier change shouldn't affect the description


        changeDescription = new ChangeDescription().id(model.id()).description("Description #2");
        repository.publish(changeDescription).get();
        assertEquals(model.description(), "Description #2");
    }
}