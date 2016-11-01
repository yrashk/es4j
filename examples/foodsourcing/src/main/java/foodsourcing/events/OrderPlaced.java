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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@Accessors(fluent = true)
public class OrderPlaced extends StandardEvent {
    private List<Order.Item> items;


    public final static SimpleIndex<OrderPlaced, UUID> ID = SimpleIndex.as(StandardEntity::uuid);


    public final static MultiValueIndex<OrderPlaced, UUID> ITEM =
            MultiValueIndex.as((object) -> object.items().stream().map(Order.Item::menuItem)
                              .collect(Collectors.toList()));
}
