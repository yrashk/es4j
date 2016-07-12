/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.protocols;

import com.eventsourcing.*;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.cep.events.Undeleted;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.queries.ModelCollectionQuery;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DeletedProtocolTest extends RepositoryUsingTest {


    public DeletedProtocolTest() {
        super(com.eventsourcing.cep.events.Deleted.class.getPackage(), DeletedProtocolTest.class.getPackage());
    }

    public static class Create extends StandardCommand<Created, Created> {
        @Override public EventStream<Created> events() throws Exception {
            Created created = new Created();
            return EventStream.ofWithState(created, created);
        }

        @Override public Created result(Created state) {
            return state;
        }
    }
    public static class Created extends StandardEvent {
        @Index
        public static SimpleAttribute<Created, UUID> ID = new SimpleAttribute<Created, UUID>
                ("id") {
            @Override public UUID getValue(Created object, QueryOptions queryOptions) {
                return object.uuid();
            }
        };

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

        public Delete(UUID id) {
            this.id = id;
        }

        @Override
        public EventStream<Void> events() throws Exception {
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

        public Undelete(UUID id) {
            this.id = id;
        }


        @Override
        public EventStream<Void> events() throws Exception {
            return EventStream.of(Undeleted.builder().deleted(id).build());
        }

        @Override
        public Void result() {
            return null;
        }
    }

    public static class TestModel implements Model, DeletedProtocol {

        @Getter
        private final Repository repository;

        @Getter
        private final UUID id;

        public TestModel(Repository repository, UUID id) {
            this.repository = repository;
            this.id = id;
        }

        public static Optional<TestModel> lookup(Repository repository, UUID id) {
            return Optional.of(new TestModel(repository, id));
        }

    }

    @Test @SneakyThrows
    public void deletion() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        assertFalse(model.deleted().isPresent());
        Delete delete = new Delete(model.getId());
        repository.publish(delete).get();
        assertTrue(model.deleted().isPresent());
    }

    @Test @SneakyThrows
    public void undeletion() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        Delete delete = new Delete(model.getId());
        repository.publish(delete).get();
        Undelete undelete = new Undelete(delete.eventId);
        repository.publish(undelete).get();
        assertFalse(model.deleted().isPresent());
    }

    @Test @SneakyThrows
    public void queryNonDeleted() {
        TestModel model = new TestModel(repository, UUID.randomUUID());

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, DeletedProtocol.deleted(TestModel::lookup));

        assertEquals(models.size(), 0);

    }


    @Test @SneakyThrows
    public void queryUndeleted() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        assertFalse(model.deleted().isPresent());
        Delete delete = new Delete(model.getId());
        repository.publish(delete).get();
        Undelete undelete = new Undelete(delete.eventId);
        repository.publish(undelete).get();

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, DeletedProtocol.deleted(TestModel::lookup));

        assertEquals(models.size(), 0);

    }

    @Test @SneakyThrows
    public void queryDeleted() {
        TestModel model = new TestModel(repository, UUID.randomUUID());
        assertFalse(model.deleted().isPresent());
        Delete delete = new Delete(model.getId());
        repository.publish(delete).get();

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, DeletedProtocol.deleted(TestModel::lookup));

        assertEquals(models.size(), 1);

    }


    @Test @SneakyThrows
    public void queryNonDeletedForNonDeleted() {
        Created created = repository.publish(new Create()).get();

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, DeletedProtocol.notDeleted(Created.class, Created.ID, TestModel::lookup));

        assertEquals(models.size(), 1);

    }

    @Test @SneakyThrows
    public void queryNonDeletedForUndeleted() {
        Created created = repository.publish(new Create()).get();

        Delete delete = new Delete(created.uuid());
        repository.publish(delete).get();
        Undelete undelete = new Undelete(delete.eventId);
        repository.publish(undelete).get();

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, DeletedProtocol.notDeleted(Created.class, Created.ID, TestModel::lookup));

        assertEquals(models.size(), 1);

    }


    @Test @SneakyThrows
    public void queryNonDeletedForDeleted() {
        Created created = repository.publish(new Create()).get();

        Delete delete = new Delete(created.uuid());
        repository.publish(delete).get();

        Collection<TestModel> models = ModelCollectionQuery
                .query(repository, DeletedProtocol.notDeleted(Created.class, Created.ID, TestModel::lookup));

        assertEquals(models.size(), 0);

    }

}