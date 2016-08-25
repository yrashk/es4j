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
import com.eventsourcing.index.MultiValueIndex;
import com.eventsourcing.index.SimpleIndex;
import foodsourcing.Order;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@Accessors(fluent = true)
public class OrderPlaced extends StandardEvent {
    private List<Order.Item> items;

    @NonFinal
    public static SimpleIndex<OrderPlaced, UUID> ID = StandardEntity::uuid;

    @NonFinal
    public static MultiValueIndex<OrderPlaced, UUID> ITEM =
            (object) -> object.items().stream().map(Order.Item::menuItem)
                              .collect(Collectors.toList());
}
