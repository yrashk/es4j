/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.protocols;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Protocol;
import com.eventsourcing.Repository;
import com.eventsourcing.queries.ModelCollectionQuery;
import com.eventsourcing.queries.ModelLoader;
import com.eventsourcing.queries.ModelQueries;
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.cep.events.Undeleted;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.logical.Not;
import com.googlecode.cqengine.resultset.ResultSet;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.eventsourcing.queries.QueryFactory.isLatestEntity;
import static com.googlecode.cqengine.query.QueryFactory.*;
import static com.eventsourcing.index.EntityQueryFactory.*;

@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface DeletedProtocol extends Protocol, ModelQueries {
    default Optional<Deleted> deleted() {
        Not<EntityHandle<Deleted>> additionalQuery = not(existsIn(getRepository().getIndexEngine()
                                                                  .getIndexedCollection(Undeleted.class),
                                                                  Deleted.ID, Undeleted.DELETED_ID));
        return latestAssociatedEntity(Deleted.class, Deleted.REFERENCE_ID, Deleted.TIMESTAMP, additionalQuery);
    }

    class DeletedModelCollectionQuery<T extends DeletedProtocol> implements ModelCollectionQuery<T> {

        private final ModelLoader<T> loader;

        public DeletedModelCollectionQuery(ModelLoader<T> loader) {
            this.loader = loader;
        }

        @Override public Stream<T> getCollectionStream(Repository repository) {
            IndexedCollection<EntityHandle<Deleted>> deletedCollection = repository.getIndexEngine()
                                                                                   .getIndexedCollection(Deleted.class);
            IndexedCollection<EntityHandle<Undeleted>> undeletedCollection = repository.getIndexEngine()
                                                                                     .getIndexedCollection(
                                                                                             Undeleted.class);

            Query<EntityHandle<Deleted>> query = and(not(existsIn(undeletedCollection, Deleted.ID, Undeleted.DELETED_ID)),
                                                     isLatestEntity(deletedCollection,
                                                                    new DeletedQueryFunction(), Deleted.TIMESTAMP));

            ResultSet<EntityHandle<Deleted>> resultSet = repository.query(Deleted.class, query);
            return StreamSupport.stream(resultSet.spliterator(), false)
                                .map(h -> loader.load(repository, h.get().reference()).get())
                                .onClose(resultSet::close);
        }

        private static class DeletedQueryFunction implements Function<EntityHandle<Deleted>, Query<EntityHandle<Deleted>>> {
            @Override public Query<EntityHandle<Deleted>> apply(EntityHandle<Deleted> v) {
                return equal(Deleted.REFERENCE_ID, v.uuid());
            }
        }
    }

    static <T extends DeletedProtocol> ModelCollectionQuery<T> deleted(ModelLoader<T> loader) {
        return new DeletedModelCollectionQuery<>(loader);
    }
}
