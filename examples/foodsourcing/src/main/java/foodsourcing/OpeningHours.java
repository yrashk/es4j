/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.eventsourcing.layout.LayoutConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalTime;

@Accessors(fluent = true)
@Value
public class OpeningHours {

    private Time from;
    private Time till;

    @Accessors(fluent = true)
    @Value
    public static class Time {
        private final int hour;
        private final int minute;

        @Override public String toString() {
            return LocalTime.of(hour, minute).toString();
        }
    }

    @LayoutConstructor
    public OpeningHours(Time from, Time till) {
        this.from = from;
        this.till = till;
    }

    public OpeningHours(int fromHour, int fromMinute, int tillHour, int tillMinute) {
        this.from = new Time(fromHour, fromMinute);
        this.till = new Time(tillHour, tillMinute);
    }
}
