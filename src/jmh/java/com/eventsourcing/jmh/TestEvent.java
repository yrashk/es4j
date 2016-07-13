/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Builder;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

public class TestEvent extends StandardEvent {
    private final String string;

    @Index({EQ})
    public static SimpleAttribute<TestEvent, String> ATTR = new SimpleAttribute<TestEvent, String>("attr") {
        @Override
        public String getValue(TestEvent object, QueryOptions queryOptions) {
            return object.string();
        }
    };

    @Builder
    public TestEvent(HybridTimestamp timestamp, String string) {
        super(timestamp);
        this.string = string;
    }

    public String toString() {
        return "RepositoryBenchmark.TestEvent(string=" + this.string + ")";
    }

    public String string() {
        return this.string;
    }

}
