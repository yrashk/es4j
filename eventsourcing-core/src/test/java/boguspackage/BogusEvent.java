/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package boguspackage;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;
import static com.eventsourcing.index.IndexEngine.IndexFeature.SC;

@Accessors(fluent = true)
public class BogusEvent extends StandardEvent {
    @Getter
    private final String string;

    @Index({EQ, SC})
    public static SimpleAttribute<BogusEvent, String> ATTR = new SimpleAttribute<BogusEvent, String>() {
        @Override
        public String getValue(BogusEvent object, QueryOptions queryOptions) {
            return object.string();
        }
    };

    @Builder
    public BogusEvent(HybridTimestamp timestamp, String string) {
        super(timestamp);
        this.string = string == null ? "bogus" : string;
    }
}
