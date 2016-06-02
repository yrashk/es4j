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
