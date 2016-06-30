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
import com.eventsourcing.hlc.HybridTimestamp;
import lombok.Builder;
import lombok.Getter;
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

        @Getter
        final private UUID id;
        private UUID eventId;

        @Builder
        public Delete(UUID id, HybridTimestamp timestamp) {
            super(timestamp);
            this.id = id;
        }


        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            Deleted reference = Deleted.builder().reference(id).build();
            eventId = reference.uuid();
            return EventStream.of(reference);
        }

        @Override
        public Void result() {
            return null;
        }
    }

    @Accessors(fluent = true)
    public static class Undelete extends StandardCommand<Void, Void> {

        @Getter
        private final UUID id;

        @Builder
        public Undelete(UUID id, HybridTimestamp timestamp) {
            super(timestamp);
            this.id = id;
        }

        @Override
        public EventStream<Void> events(Repository repository) throws Exception {
            return EventStream.of(Undeleted.builder().deleted(id).build());
        }

        @Override
        public Void result() {
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
        Delete delete = Delete.builder().id(model.id()).build();
        repository.publish(delete).get();
        assertTrue(model.deleted().isPresent());
    }

    @Test @SneakyThrows
    public void undeletion() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        Delete delete = Delete.builder().id(model.id()).build();
        repository.publish(delete).get();
        Undelete undelete = Undelete.builder().id(delete.eventId).build();
        repository.publish(undelete).get();
        assertFalse(model.deleted().isPresent());
    }

}