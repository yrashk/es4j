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
import foodsourcing.events.RestaurantConfirmedOrder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.UUID;

@Value
@Accessors(fluent = true)
public class RequestRestaurantOrderConfirmation extends StandardCommand<Void, Void> {

    private UUID order;

    @Override public EventStream<Void> events() throws Exception {
        return EventStream.of(new RestaurantConfirmedOrder(order, new ArrayList<>()));
    }
}
