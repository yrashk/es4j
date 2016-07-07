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
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import foodsourcing.Address;
import foodsourcing.utils.GeoLocation;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;
import static com.eventsourcing.index.IndexEngine.IndexFeature.GT;
import static com.eventsourcing.index.IndexEngine.IndexFeature.LT;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class AddressChanged extends StandardEvent {
    private UUID reference;
    private Address address;

    private static double EARTH_RADIUS_KM = 6371.01;
    private static final double DISTANCE_10_KM = 10.0;

    private GeoLocation[] boundingCoordinates(double distance) {
        GeoLocation geoLocation = GeoLocation.fromDegrees(address().latitude(), address().longitude());
        return geoLocation.boundingCoordinates(distance, EARTH_RADIUS_KM);
    }

    @Index
    public static SimpleAttribute<AddressChanged, UUID> ID = new SimpleAttribute<AddressChanged, UUID>("id") {
        @Override public UUID getValue(AddressChanged object, QueryOptions queryOptions) {
            return object.uuid();
        }
    };

    @Index
    public static SimpleAttribute<AddressChanged, UUID> REFERENCE_ID = new SimpleAttribute<AddressChanged, UUID>("referenceId") {
        @Override public UUID getValue(AddressChanged object, QueryOptions queryOptions) {
            return object.reference();
        }
    };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<AddressChanged, HybridTimestamp> TIMESTAMP =
            new SimpleAttribute<AddressChanged, HybridTimestamp>("timestamp") {
        @Override public HybridTimestamp getValue(AddressChanged object, QueryOptions queryOptions) {
            return object.timestamp();
        }
    };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<AddressChanged, Double> BOUNDING_BOX_10K_LAT_START =
            new SimpleAttribute<AddressChanged, Double>
            ("boundingBox10K_lat") {
        @Override public Double getValue(AddressChanged object, QueryOptions queryOptions) {
            return object.boundingCoordinates(DISTANCE_10_KM)[0].getLatitudeInDegrees();
        }
    };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<AddressChanged, Double> BOUNDING_BOX_10K_LONG_START =
            new SimpleAttribute<AddressChanged, Double>("boundingBox10K_long") {
        @Override public Double getValue(AddressChanged object, QueryOptions queryOptions) {
            return object.boundingCoordinates(DISTANCE_10_KM)[0].getLongitudeInDegrees();
        }
    };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<AddressChanged, Double> BOUNDING_BOX_10K_LAT_END =
            new SimpleAttribute<AddressChanged, Double>
                    ("boundingBox10K_lat_end") {
                @Override public Double getValue(AddressChanged object, QueryOptions queryOptions) {
                    return object.boundingCoordinates(DISTANCE_10_KM)[1].getLatitudeInDegrees();
                }
            };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<AddressChanged, Double> BOUNDING_BOX_10K_LONG_END =
            new SimpleAttribute<AddressChanged, Double>("boundingBox10K_long_end") {
                @Override public Double getValue(AddressChanged object, QueryOptions queryOptions) {
                    return object.boundingCoordinates(DISTANCE_10_KM)[1].getLongitudeInDegrees();
                }
            };

}
