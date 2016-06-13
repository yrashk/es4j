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
import com.eventsourcing.cep.events.DescriptionChanged;
import com.googlecode.cqengine.query.option.EngineThresholds;
import com.googlecode.cqengine.resultset.ResultSet;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Draft;

import static com.googlecode.cqengine.query.QueryFactory.*;

@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface DescriptionProtocol extends Protocol {
    default String description() {
        try (ResultSet<EntityHandle<DescriptionChanged>> resultSet =
                     getRepository().query(DescriptionChanged.class, equal(DescriptionChanged.REFERENCE_ID, id()),
                                           queryOptions(orderBy(descending(DescriptionChanged.TIMESTAMP)),
                                                        applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
            if (resultSet.isEmpty()) {
                return null;
            }
            return resultSet.iterator().next().get().description();
        }
    }
}
