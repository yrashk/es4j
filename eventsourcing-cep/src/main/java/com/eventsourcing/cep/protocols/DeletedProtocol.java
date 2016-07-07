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
import com.eventsourcing.queries.ModelQueries;
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.cep.events.Undeleted;
import com.googlecode.cqengine.query.logical.Not;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.Optional;

import static com.googlecode.cqengine.query.QueryFactory.*;

@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface DeletedProtocol extends Protocol, ModelQueries {
    default Optional<Deleted> deleted() {
        Not<EntityHandle<Deleted>> additionalQuery = not(existsIn(getRepository().getIndexEngine()
                                                                  .getIndexedCollection(Undeleted.class),
                                                                  Deleted.ID, Undeleted.DELETED_ID));
        return latestAssociatedEntity(Deleted.class, Deleted.REFERENCE_ID, Deleted.TIMESTAMP, additionalQuery);
    }
}
