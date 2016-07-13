/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Index;
import com.eventsourcing.index.SimpleIndex;
import foodsourcing.Address;
import foodsourcing.utils.GeoLocation;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

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

    @NonFinal
    public static SimpleIndex<AddressChanged, UUID> ID = (addressChanged, queryOptions) -> addressChanged.uuid();

    @NonFinal
    public static SimpleIndex<AddressChanged, UUID> REFERENCE_ID = (addressChanged, queryOptions) -> addressChanged
            .reference();

    @NonFinal
    @Index({EQ, LT, GT})
    public static SimpleIndex<AddressChanged, HybridTimestamp> TIMESTAMP =
            (addressChanged, queryOptions) -> addressChanged.timestamp();

    @NonFinal
    @Index({EQ, LT, GT})
    public static SimpleIndex<AddressChanged, Double> BOUNDING_BOX_10K_LAT_START =
            (addressChanged, queryOptions) -> addressChanged.boundingCoordinates(DISTANCE_10_KM)[0].getLatitudeInDegrees();

    @NonFinal
    @Index({EQ, LT, GT})
    public static SimpleIndex<AddressChanged, Double> BOUNDING_BOX_10K_LONG_START =
            (addressChanged, queryOptions) -> addressChanged.boundingCoordinates(DISTANCE_10_KM)[0].getLongitudeInDegrees();

    @NonFinal
    @Index({EQ, LT, GT})
    public static SimpleIndex<AddressChanged, Double> BOUNDING_BOX_10K_LAT_END =
            (addressChanged, queryOptions) -> addressChanged.boundingCoordinates(DISTANCE_10_KM)[1].getLatitudeInDegrees();

    @NonFinal
    @Index({EQ, LT, GT})
    public static SimpleIndex<AddressChanged, Double> BOUNDING_BOX_10K_LONG_END =
            (addressChanged, queryOptions) -> addressChanged.boundingCoordinates(DISTANCE_10_KM)[1].getLongitudeInDegrees();

}
