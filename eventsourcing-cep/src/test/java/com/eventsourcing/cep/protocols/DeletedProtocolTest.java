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
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.cep.events.Undeleted;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DeletedProtocolTest extends RepositoryTest {


    public DeletedProtocolTest() {
        super(com.eventsourcing.cep.events.Deleted.class.getPackage(), DeletedProtocolTest.class.getPackage());
    }

    @Accessors(fluent = true)
    public static class Delete extends StandardCommand<Void, Void> {

        @Getter @Setter
        private UUID id;
        private UUID eventId;

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            Deleted reference = new Deleted().reference(id);
            eventId = reference.uuid();
            return EventStream.of(reference);
        }

        @Override
        public Void onCompletion() {
            return null;
        }
    }

    @Accessors(fluent = true)
    public static class Undelete extends StandardCommand<Void, Void> {

        @Getter @Setter
        private UUID id;

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            return EventStream.of(new Undeleted().deleted(id));
        }

        @Override
        public Void onCompletion() {
            return null;
        }
    }

    @Accessors(fluent = true)
    public static class TestModel implements Model, DeletedProtocol {

        @Getter @Accessors(fluent = false)
        private final Repository repository;

        @Getter
        private final UUID id;

        public TestModel(Repository repository, UUID id) {
            this.repository = repository;
            this.id = id;
        }

    }

    @Test @SneakyThrows
    public void deletion() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        assertFalse(model.deleted().isPresent());
        Delete delete = new Delete().id(model.id());
        repository.publish(delete).get();
        assertTrue(model.deleted().isPresent());
    }

    @Test @SneakyThrows
    public void undeletion() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        Delete delete = new Delete().id(model.id());
        repository.publish(delete).get();
        Undelete undelete = new Undelete().id(delete.eventId);
        repository.publish(undelete).get();
        assertFalse(model.deleted().isPresent());
    }

}