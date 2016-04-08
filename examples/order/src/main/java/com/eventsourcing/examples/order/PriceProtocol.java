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
package com.eventsourcing.examples.order;

import com.eventsourcing.Protocol;
import com.eventsourcing.examples.order.events.PriceChanged;

import java.math.BigDecimal;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public interface PriceProtocol extends Protocol, QueryUtilities {
    default BigDecimal price() {
        return last(getRepository(), PriceChanged.class, equal(PriceChanged.REFERENCE_ID, id()), PriceChanged.TIMESTAMP).
                orElse(new PriceChanged(null, BigDecimal.ZERO)).price();
    }
}
