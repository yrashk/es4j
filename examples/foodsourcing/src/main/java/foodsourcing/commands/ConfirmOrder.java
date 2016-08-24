/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.StandardCommand;
import foodsourcing.events.OrderConfirmed;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

@Value
@Accessors(fluent = true)
public class ConfirmOrder extends StandardCommand<Void, Void> {
    UUID reference;

    @Override public EventStream<Void> events() throws Exception {
        return EventStream.of(new OrderConfirmed(reference));
    }
}
