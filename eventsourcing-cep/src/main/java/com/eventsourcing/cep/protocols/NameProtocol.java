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
import com.eventsourcing.cep.events.NameChanged;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.Optional;

@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface NameProtocol extends Protocol, ModelQueries {
    default String name() {
        Optional<NameChanged> last = latestAssociatedEntity(NameChanged.class, NameChanged.REFERENCE_ID, NameChanged.TIMESTAMP);
        if (last.isPresent()) {
            return last.get().name();
        } else {
            return null;
        }
    }
}
