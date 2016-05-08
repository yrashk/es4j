/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order;

import com.eventsourcing.Protocol;
import com.eventsourcing.examples.order.events.NameChanged;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public interface NameProtocol extends Protocol, QueryUtilities {

    default String name() {
        return last(getRepository(), NameChanged.class, equal(NameChanged.REFERENCE_ID, id()), NameChanged.TIMESTAMP).
                                                                                                                             orElse(new NameChanged(
                                                                                                                                     null,
                                                                                                                                     "Unnamed"))
                                                                                                                     .name();
    }
}
