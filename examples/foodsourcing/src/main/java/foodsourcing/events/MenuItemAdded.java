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
import com.eventsourcing.index.SimpleIndex;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.util.UUID;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class MenuItemAdded extends StandardEvent {

    private UUID reference;


    public final static SimpleIndex<MenuItemAdded, UUID> ID = SimpleIndex.as(StandardEntity::uuid);


    public final static SimpleIndex<MenuItemAdded, UUID> REFERENCE_ID = SimpleIndex.as(MenuItemAdded::reference);
}
