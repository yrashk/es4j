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
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.cep.events.Undeleted;
import com.googlecode.cqengine.query.option.EngineThresholds;
import com.googlecode.cqengine.resultset.ResultSet;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.Optional;

import static com.googlecode.cqengine.query.QueryFactory.*;

@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface DeletedProtocol extends Protocol {
    default Optional<Deleted> deleted() {
        try (ResultSet<EntityHandle<Deleted>> resultSet =
                     getRepository().query(Deleted.class,
                                           and(equal(Deleted.REFERENCE_ID, id()),
                                               not(existsIn(getRepository().getIndexEngine()
                                                                       .getIndexedCollection(Undeleted.class),
                                                        Deleted.ID, Undeleted.DELETED_ID))),
                                           queryOptions(orderBy(descending(Deleted.TIMESTAMP)),
                                                        applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(resultSet.iterator().next().get());
        }
    }
}
