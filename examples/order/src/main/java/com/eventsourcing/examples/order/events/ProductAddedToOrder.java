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
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProductAddedToOrder extends StandardEvent {
    @Getter
    @Setter
    private UUID orderId;

    @Getter @Setter
    private UUID productId;

    @Getter @Setter
    private int quantity;

    @Index({EQ, UNIQUE})
    public static final SimpleAttribute<ProductAddedToOrder, UUID> ID = new SimpleAttribute<ProductAddedToOrder, UUID>(
            "id") {
        public UUID getValue(ProductAddedToOrder productAddedToOrder, QueryOptions queryOptions) {
            return productAddedToOrder.uuid();
        }
    };

    @Index({EQ})
    public static final SimpleAttribute<ProductAddedToOrder, UUID> ORDER_ID = new SimpleAttribute<ProductAddedToOrder, UUID>(
            "orderId") {
        public UUID getValue(ProductAddedToOrder productAddedToOrder, QueryOptions queryOptions) {
            return productAddedToOrder.orderId();
        }
    };

    @Index({EQ})
    public static final SimpleAttribute<ProductAddedToOrder, UUID> PRODUCT_ID = new SimpleAttribute<ProductAddedToOrder, UUID>(
            "productId") {
        public UUID getValue(ProductAddedToOrder productAddedToOrder, QueryOptions queryOptions) {
            return productAddedToOrder.productId();
        }
    };

    @Index({EQ, LT, GT})
    public static final SimpleAttribute<ProductAddedToOrder, HybridTimestamp> TIMESTAMP = new SimpleAttribute<ProductAddedToOrder, HybridTimestamp>(
            "timestamp") {
        public HybridTimestamp getValue(ProductAddedToOrder productAddedToOrder, QueryOptions queryOptions) {
            return productAddedToOrder.timestamp();
        }
    };
}
