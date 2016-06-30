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
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class NameProtocolTest extends RepositoryTest {

    public NameProtocolTest() {
        super(NameChanged.class.getPackage(), NameProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class Rename extends StandardCommand<Void, String> {

        @Getter
        private final UUID id;
        @Getter
        private final String name;

        @Builder
        public Rename(HybridTimestamp timestamp, UUID id, String name) {
            super(timestamp);
            this.id = id;
            this.name = name;
        }

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            return EventStream.of(NameChanged.builder().reference(id).name(name).timestamp(timestamp()).build());
        }

        @Override
        public String result() {
            return name;
        }
    }

    @Accessors(fluent = true)
    public static class TestModel implements Model, NameProtocol {

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
    public void renaming() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        TestModel model = new TestModel(repository, UUID.randomUUID());

        Rename rename = Rename.builder().id(model.id()).name("Name #1").build();
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #1");

        Rename renameBefore = Rename.builder().id(model.id()).name("Name #0").timestamp(timestamp).build();
        assertTrue(renameBefore.timestamp().compareTo(rename.timestamp()) < 0);
        repository.publish(renameBefore).get();
        assertEquals(model.name(), "Name #1"); // earlier change shouldn't affect the name


        rename = Rename.builder().id(model.id()).name("Name #2").build();
        repository.publish(rename).get();
        assertEquals(model.name(), "Name #2");
    }
}