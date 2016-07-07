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
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class MenuItemAdded extends StandardEvent {

    private UUID reference;

    @Index
    public static SimpleAttribute<MenuItemAdded, UUID> ID = new SimpleAttribute<MenuItemAdded, UUID>("id") {
        @Override public UUID getValue(MenuItemAdded object, QueryOptions queryOptions) {
            return object.uuid();
        }
    };

    @Index
    public static SimpleAttribute<MenuItemAdded, UUID> REFERENCE_ID = new SimpleAttribute<MenuItemAdded, UUID>("referenceId") {
        @Override public UUID getValue(MenuItemAdded object, QueryOptions queryOptions) {
            return object.reference();
        }
    };
}
