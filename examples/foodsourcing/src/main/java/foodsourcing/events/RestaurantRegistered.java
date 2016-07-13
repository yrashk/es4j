/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.index.SimpleIndex;

import java.util.UUID;

public class RestaurantRegistered extends StandardEvent {

    public static SimpleIndex<RestaurantRegistered, UUID> ID =
            (restaurantRegistered, queryOptions) -> restaurantRegistered.uuid();

}
