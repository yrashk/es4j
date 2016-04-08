/**
 * Copyright 2016 Eventsourcing team
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
package com.eventsourcing.examples.order.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class OrderCancelled extends Event {

    @Getter @Setter
    private UUID id;

    @Index({EQ})
    public static final SimpleAttribute<OrderCancelled, UUID> REFERENCE_ID = new SimpleAttribute<OrderCancelled, UUID>("referenceId") {
        public UUID getValue(OrderCancelled orderCancelled, QueryOptions queryOptions) {
            return orderCancelled.id();
        }
    };
}
