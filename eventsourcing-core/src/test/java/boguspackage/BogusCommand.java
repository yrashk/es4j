/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package boguspackage;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.hlc.HybridTimestamp;
import lombok.Builder;

public class BogusCommand extends StandardCommand<String, Void> {

    @Builder
    public BogusCommand(HybridTimestamp timestamp) {
        super(timestamp);
    }

    @Override
    public EventStream<Void> events(Repository repository) throws Exception {
        return EventStream.of(BogusEvent.builder().build());
    }

    @Override
    public String result() {
        return "bogus";
    }
}
