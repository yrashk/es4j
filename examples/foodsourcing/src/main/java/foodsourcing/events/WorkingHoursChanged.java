/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEntity;
import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Index;
import com.eventsourcing.index.MultiValueIndex;
import com.eventsourcing.index.SimpleIndex;
import com.eventsourcing.layout.SerializableComparable;
import foodsourcing.OpeningHours;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class WorkingHoursChanged extends StandardEvent {
    private UUID reference;
    private DayOfWeek dayOfWeek;
    private List<OpeningHours> openDuring;

    @NonFinal
    public static SimpleIndex<WorkingHoursChanged, UUID> REFERENCE_ID = WorkingHoursChanged::reference;

    @NonFinal
    @Index({EQ, LT, GT})
    public static SimpleIndex<WorkingHoursChanged, HybridTimestamp> TIMESTAMP = StandardEntity::timestamp;

    @Value
    public static class OpeningHoursBoundary
            implements Comparable<OpeningHoursBoundary>, SerializableComparable<Integer> {
        private DayOfWeek dayOfWeek;
        private OpeningHours.Time time;

        @Override public int compareTo(OpeningHoursBoundary o) {
            int dayComparison = dayOfWeek.compareTo(o.dayOfWeek);
            if (dayComparison == 0) {
                if (time.hour() < o.time.hour()) {
                    return -1;
                }
                if (time.hour() == o.time.hour() && time.minute() < o.time.minute()) {
                    return -1;
                }
                if (time.hour() > o.time.hour()) {
                    return 1;
                }
                if (time.minute() > o.time.minute()) {
                    return 1;
                }
                return 0;
            } else {
                return dayComparison;
            }
        }

        @Override public Integer getSerializableComparable() {
            return (dayOfWeek.getValue() - 1) * 24 * 60 + time.hour() * 60 + time.minute();
        }
    }

    @NonFinal
    @Index({EQ, LT, GT})
    public static MultiValueIndex<WorkingHoursChanged, OpeningHoursBoundary> OPENING_AT =
            (workingHoursChanged) ->
                    workingHoursChanged.openDuring().stream()
                            .map(openingHours ->
                                              new OpeningHoursBoundary(workingHoursChanged.dayOfWeek(), openingHours.from()))
                            .collect(Collectors.toList());

    @NonFinal
    @Index({EQ, LT, GT})
    public static MultiValueIndex<WorkingHoursChanged, OpeningHoursBoundary> CLOSING_AT =
            (workingHoursChanged) ->
                    workingHoursChanged.openDuring().stream()
                                 .map(openingHours ->
                                              new OpeningHoursBoundary(workingHoursChanged.dayOfWeek(), openingHours.till()))
                                 .collect(Collectors.toList());
    @NonFinal
    public static SimpleIndex<WorkingHoursChanged, DayOfWeek> DAY_OF_WEEK = WorkingHoursChanged::dayOfWeek;

}
