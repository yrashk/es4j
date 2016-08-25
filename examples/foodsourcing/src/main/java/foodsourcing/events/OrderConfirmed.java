/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.index.SimpleIndex;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.util.UUID;

@Value
@Accessors(fluent = true)
public class OrderConfirmed extends StandardEvent {
    UUID reference;

    @NonFinal
    public static SimpleIndex<OrderConfirmed, UUID> REFERENCE = OrderConfirmed::reference;
}
