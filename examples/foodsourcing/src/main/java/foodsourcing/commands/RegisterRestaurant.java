/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.cep.events.NameChanged;
import foodsourcing.Address;
import foodsourcing.OpeningHours;
import foodsourcing.Restaurant;
import foodsourcing.events.AddressChanged;
import foodsourcing.events.RestaurantRegistered;
import foodsourcing.events.WorkingHoursChanged;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class RegisterRestaurant extends StandardCommand<RestaurantRegistered, Restaurant> {

    private String name;
    private Address address;
    private OpeningHours openDuring;

    @Override public EventStream<RestaurantRegistered> events() throws Exception {
        RestaurantRegistered restaurantRegistered = new RestaurantRegistered();
        NameChanged nameChanged = new NameChanged(restaurantRegistered.uuid(), name);
        AddressChanged addressChanged = new AddressChanged(restaurantRegistered.uuid(), address);
        Stream<WorkingHoursChanged> workingHoursChangedStream =
                Arrays.asList(DayOfWeek.values()).stream()
                      .map(dayOfWeek -> new WorkingHoursChanged(restaurantRegistered.uuid(),
                                                                dayOfWeek, Collections.singletonList(openDuring)));
        return EventStream.ofWithState(restaurantRegistered,
                                       Stream.concat(
                                         Stream.of(restaurantRegistered, nameChanged, addressChanged),
                                         workingHoursChangedStream
                                       ));
    }

    @Override public Restaurant result(RestaurantRegistered restaurantRegistered, Repository repository) {
        return Restaurant.lookup(repository, restaurantRegistered.uuid()).get();
    }
}
