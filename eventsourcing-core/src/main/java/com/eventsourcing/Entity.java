/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.LayoutIgnore;

import java.util.UUID;

public interface Entity<E extends Entity> {
    @LayoutIgnore UUID uuid();
    E uuid(UUID uuid);
    HybridTimestamp timestamp();
    E timestamp(HybridTimestamp timestamp);
}
