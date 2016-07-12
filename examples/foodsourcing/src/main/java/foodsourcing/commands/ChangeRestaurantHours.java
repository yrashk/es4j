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
import com.eventsourcing.layout.LayoutConstructor;
import foodsourcing.OpeningHours;
import foodsourcing.Restaurant;
import foodsourcing.events.WorkingHoursChanged;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@ToString
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class ChangeRestaurantHours extends StandardCommand<Void, Void> {
    @Getter
    private final UUID restaurantId;
    @Getter
    private final DayOfWeek dayOfWeek;
    @Getter
    private final List<OpeningHours> openDuring;

    @LayoutConstructor
    public ChangeRestaurantHours(UUID restaurantId, DayOfWeek dayOfWeek,
                                 List<OpeningHours> openDuring) {
        this.restaurantId = restaurantId;
        this.dayOfWeek = dayOfWeek;
        this.openDuring = openDuring;
    }

    public ChangeRestaurantHours(Restaurant restaurant, DayOfWeek dayOfWeek,
                                 List<OpeningHours> openDuring) {
        this.restaurantId = restaurant.getId();
        this.dayOfWeek = dayOfWeek;
        this.openDuring = openDuring;
    }

    @Override public EventStream<Void> events() throws Exception {
        return EventStream.of(new WorkingHoursChanged(restaurantId, dayOfWeek, openDuring));
    }
}
