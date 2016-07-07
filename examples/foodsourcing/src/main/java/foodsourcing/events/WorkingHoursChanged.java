/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.MultiValueAttribute;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import foodsourcing.OpeningHours;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;
import com.eventsourcing.layout.SerializableComparable;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class WorkingHoursChanged extends StandardEvent {
    private UUID reference;
    private DayOfWeek dayOfWeek;
    private List<OpeningHours> openDuring;

    @Index
    public static SimpleAttribute<WorkingHoursChanged, UUID> REFERENCE_ID = new SimpleAttribute<WorkingHoursChanged, UUID>("referenceId") {
        @Override public UUID getValue(WorkingHoursChanged object, QueryOptions queryOptions) {
            return object.reference();
        }
    };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<WorkingHoursChanged, HybridTimestamp> TIMESTAMP =
            new SimpleAttribute<WorkingHoursChanged, HybridTimestamp>("timestamp") {
                @Override public HybridTimestamp getValue(WorkingHoursChanged object, QueryOptions queryOptions) {
                    return object.timestamp();
                }
            };

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

    @Index({EQ, LT, GT})
    public static MultiValueAttribute<WorkingHoursChanged, OpeningHoursBoundary> OPENING_AT =
            new MultiValueAttribute<WorkingHoursChanged, OpeningHoursBoundary>("openingAt") {
                @Override public Iterable<OpeningHoursBoundary> getValues(WorkingHoursChanged object,
                                                                          QueryOptions queryOptions) {
                    return object.openDuring().stream()
                            .map(openingHours ->
                                              new OpeningHoursBoundary(object.dayOfWeek(), openingHours.from()))
                            .collect(Collectors.toList());
                }
            };

    @Index({EQ, LT, GT})
    public static MultiValueAttribute<WorkingHoursChanged, OpeningHoursBoundary> CLOSING_AT =
            new MultiValueAttribute<WorkingHoursChanged, OpeningHoursBoundary>("closingAt") {
                @Override public Iterable<OpeningHoursBoundary> getValues(WorkingHoursChanged object,
                                                                          QueryOptions queryOptions) {
                    return object.openDuring().stream()
                                 .map(openingHours ->
                                              new OpeningHoursBoundary(object.dayOfWeek(), openingHours.till()))
                                 .collect(Collectors.toList());
                }
            };
    @Index
    public static SimpleAttribute<WorkingHoursChanged, DayOfWeek> DAY_OF_WEEK =
            new SimpleAttribute<WorkingHoursChanged, DayOfWeek>("dayOfWeek") {
                @Override public DayOfWeek getValue(WorkingHoursChanged object, QueryOptions queryOptions) {
                    return object.dayOfWeek();
                }
            };

}
