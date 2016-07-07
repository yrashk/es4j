/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.layout.LayoutConstructor;
import foodsourcing.MenuItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@ToString
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class RemoveMenuItem extends StandardCommand<Void, Void> {

    @Getter
    private final UUID menuItemId;

    @LayoutConstructor
    public RemoveMenuItem(UUID menuItemId) {this.menuItemId = menuItemId;}

    public RemoveMenuItem(MenuItem menuItem) {this.menuItemId = menuItem.getId();}

    @Override public EventStream<Void> events() throws Exception {
        return EventStream.of(new Deleted(menuItemId));
    }
}
