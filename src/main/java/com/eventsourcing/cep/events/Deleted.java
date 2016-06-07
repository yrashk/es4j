/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.cep.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.layout.LayoutName;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.UUID;

/**
 * This event signifies deletion of a referenced instance.
 */
@Accessors(fluent = true)
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("http://rfc.eventsourcing.com/spec:3/CEP/#Deleted")
public class Deleted extends Event {
    @Getter @Setter
    UUID reference;

    @Index
    public static SimpleAttribute<Deleted, UUID> ID = new SimpleAttribute<Deleted, UUID>
            ("id") {
        @Override public UUID getValue(Deleted deleted, QueryOptions queryOptions) {
            return deleted.uuid();
        }
    };

    @Index
    public static SimpleAttribute<Deleted, UUID> REFERENCE_ID = new SimpleAttribute<Deleted, UUID>
            ("reference_id") {
        @Override public UUID getValue(Deleted deleted, QueryOptions queryOptions) {
            return deleted.reference();
        }
    };

    @Index
    public static SimpleAttribute<Deleted, HybridTimestamp> TIMESTAMP = new SimpleAttribute<Deleted, HybridTimestamp>
            ("timestamp") {
        @Override public HybridTimestamp getValue(Deleted deleted, QueryOptions queryOptions) {
            return deleted.timestamp();
        }
    };

}
