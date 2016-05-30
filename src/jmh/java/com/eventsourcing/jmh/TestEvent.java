/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

public class TestEvent extends Event {
    private String string;

    @Index({EQ})
    public static SimpleAttribute<TestEvent, String> ATTR = new SimpleAttribute<TestEvent, String>("attr") {
        @Override
        public String getValue(TestEvent object, QueryOptions queryOptions) {
            return object.string();
        }
    };

    public String toString() {
        return "RepositoryBenchmark.TestEvent(string=" + this.string + ")";
    }

    public String string() {
        return this.string;
    }

    public TestEvent string(String string) {
        this.string = string;
        return this;
    }
}
