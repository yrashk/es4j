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
import com.eventsourcing.layout.LayoutConstructor;
import foodsourcing.Address;
import foodsourcing.Restaurant;
import foodsourcing.events.AddressChanged;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@ToString
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class UpdateRestaurantAddress extends StandardCommand<Void, Void> {
    @Getter
    private final UUID restaurantId;
    @Getter
    private final Address address;

    @LayoutConstructor
    public UpdateRestaurantAddress(UUID restaurantId, Address address) {
        this.restaurantId = restaurantId;
        this.address = address;
    }

    public UpdateRestaurantAddress(Restaurant restaurant, Address address) {
        this.restaurantId = restaurant.getId();
        this.address = address;
    }

    @Override public EventStream<Void> events() throws Exception {
        return EventStream.of(new AddressChanged(restaurantId, address));
    }
}
