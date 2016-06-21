/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.Indexing;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.layout.LayoutName;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.Draft;
import org.unprotocols.coss.RFC;

import java.util.UUID;

/**
 * This event signifies undeletion of a referenced deletion.
 */
@Accessors(fluent = true)
@Draft @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("http://rfc.eventsourcing.com/spec:3/CEP/#Undeleted")
public class Undeleted extends StandardEvent {
    @Getter(onMethod = @__(@Index)) @Setter
    UUID deleted;

    public static Attribute<Undeleted, UUID> DELETED_ID = Indexing.getAttribute(Undeleted.class, "deleted");

    @Index
    public static SimpleAttribute<Undeleted, HybridTimestamp> TIMESTAMP = new SimpleAttribute<Undeleted, HybridTimestamp>
            ("timestamp") {
        @Override public HybridTimestamp getValue(Undeleted undeleted, QueryOptions queryOptions) {
            return undeleted.timestamp();
        }
    };

}
