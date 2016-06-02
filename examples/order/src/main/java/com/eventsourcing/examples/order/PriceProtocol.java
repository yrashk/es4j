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
