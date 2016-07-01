/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Builder;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;
import static com.eventsourcing.index.IndexEngine.IndexFeature.UNIQUE;

public class OrderCreated extends StandardEvent {
    @Index({EQ, UNIQUE})
    public static final SimpleAttribute<OrderCreated, UUID> ID = new SimpleAttribute<OrderCreated, UUID>("id") {
        public UUID getValue(OrderCreated orderCreated, QueryOptions queryOptions) {
            return orderCreated.uuid();
        }
    };


    @Builder
    public OrderCreated() {

    }
}
