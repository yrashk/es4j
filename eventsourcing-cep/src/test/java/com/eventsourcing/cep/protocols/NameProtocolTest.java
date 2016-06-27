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
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class NameProtocolTest extends RepositoryTest {

    public NameProtocolTest() {
        super(NameChanged.class.getPackage(), NameProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class Rename extends StandardCommand<String, Void> {

        @Getter @Setter
        private UUID id;
        @Getter @Setter
        private String name;

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            return EventStream.of(new NameChanged().reference(id).name(name).timestamp(timestamp()));
        }

        @Override
        public String onCompletion() {
            return name;
        }
    }

    @Accessors
    public static class TestModel implements Model, NameProtocol {

        @Getter
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
    public void renaming() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestModel model = new TestModel(repository, UUID.randomUUID());

        Rename rename = new Rename().id(model.getId()).name("Name #1");
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #1");

        Rename renameBefore = (Rename) new Rename().id(model.getId()).name("Name #0").timestamp(timestamp);
        assertTrue(renameBefore.timestamp().compareTo(rename.timestamp()) < 0);
        repository.publish(renameBefore).get();
        assertEquals(model.name(), "Name #1"); // earlier change shouldn't affect the name


        rename = new Rename().id(model.getId()).name("Name #2");
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #2");
    }
}