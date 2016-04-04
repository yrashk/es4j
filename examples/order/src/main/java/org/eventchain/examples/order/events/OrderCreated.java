/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain.examples.order.events;

import com.googlecode.cqengine.query.option.QueryOptions;
import org.eventchain.Event;
import org.eventchain.annotations.Index;
import org.eventchain.index.SimpleAttribute;

import java.util.UUID;

import static org.eventchain.index.IndexEngine.IndexFeature.EQ;
import static org.eventchain.index.IndexEngine.IndexFeature.UNIQUE;

public class OrderCreated extends Event {
    @Index({EQ, UNIQUE})
    public static final SimpleAttribute<OrderCreated, UUID> ID = new SimpleAttribute<OrderCreated, UUID>("id") {
        public UUID getValue(OrderCreated orderCreated, QueryOptions queryOptions) {
            return orderCreated.uuid();
        }
    };
}
