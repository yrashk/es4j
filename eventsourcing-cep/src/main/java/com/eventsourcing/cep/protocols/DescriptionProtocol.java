/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.protocols;

import com.eventsourcing.Protocol;
import com.eventsourcing.queries.ModelQueries;
import com.eventsourcing.cep.events.DescriptionChanged;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.Optional;

@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface DescriptionProtocol extends Protocol, ModelQueries {
    default String description() {
        Optional<DescriptionChanged> last = latestAssociatedEntity(DescriptionChanged.class, DescriptionChanged.REFERENCE_ID, DescriptionChanged.TIMESTAMP);
        if (last.isPresent()) {
            return last.get().description();
        } else {
            return null;
        }
    }
}
