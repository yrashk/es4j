/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class PriceChanged extends Event {
    @Getter @Setter
    private UUID id;
    @Getter @Setter
    private BigDecimal price;

    @Index({EQ})
    public static final SimpleAttribute<PriceChanged, UUID> REFERENCE_ID = new SimpleAttribute<PriceChanged, UUID>(
            "referenceId") {
        public UUID getValue(PriceChanged priceChanged, QueryOptions queryOptions) {
            return priceChanged.id();
        }
    };

    @Index({EQ, LT, GT})
    public static final SimpleAttribute<PriceChanged, HybridTimestamp> TIMESTAMP = new SimpleAttribute<PriceChanged, HybridTimestamp>(
            "timestamp") {
        public HybridTimestamp getValue(PriceChanged priceChanged, QueryOptions queryOptions) {
            return priceChanged.timestamp();
        }
    };
}
