/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;

/**
 * Standard {@link Event} implementation. Doesn't add any functionality.
 */
public abstract class StandardEvent extends StandardEntity<Event> implements Event {
    public StandardEvent(HybridTimestamp timestamp) {
        super(timestamp);
    }
}
