/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order;

import com.eventsourcing.Protocol;
import com.eventsourcing.examples.order.events.PriceChanged;

import java.math.BigDecimal;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public interface PriceProtocol extends Protocol, QueryUtilities {
    default BigDecimal price() {
        return last(getRepository(), PriceChanged.class, equal(PriceChanged.REFERENCE_ID, id()), PriceChanged.TIMESTAMP)
                .
                        orElse(new PriceChanged(null, BigDecimal.ZERO)).price();
    }
}
