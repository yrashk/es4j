package com.eventsourcing.examples.order.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;
import static com.eventsourcing.index.IndexEngine.IndexFeature.UNIQUE;

public class ProductCreated extends Event {

    @Index({EQ, UNIQUE})
    public static final SimpleAttribute<ProductCreated, UUID> ID = new SimpleAttribute<ProductCreated, UUID>("id") {
        public UUID getValue(ProductCreated productCreated, QueryOptions queryOptions) {
            return productCreated.uuid();
        }
    };

}
