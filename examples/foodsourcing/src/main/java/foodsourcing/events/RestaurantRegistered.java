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
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.UUID;

public class RestaurantRegistered extends StandardEvent {

    @Index
    public static SimpleAttribute<RestaurantRegistered, UUID> ID = new SimpleAttribute<RestaurantRegistered, UUID>("id") {
        @Override public UUID getValue(RestaurantRegistered object, QueryOptions queryOptions) {
            return object.uuid();
        }
    };

}
