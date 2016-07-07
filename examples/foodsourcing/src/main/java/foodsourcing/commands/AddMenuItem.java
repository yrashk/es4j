/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.cep.events.DescriptionChanged;
import com.eventsourcing.cep.events.NameChanged;
import com.eventsourcing.layout.LayoutConstructor;
import foodsourcing.MenuItem;
import foodsourcing.Restaurant;
import foodsourcing.events.MenuItemAdded;
import foodsourcing.events.PictureChanged;
import foodsourcing.events.PriceChanged;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@ToString
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class AddMenuItem extends StandardCommand<MenuItemAdded, MenuItem> {
    @Getter
    private final UUID restaurantId;
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final String pictureContentType;
    @Getter
    private final byte[] picture;
    @Getter
    private final BigDecimal price;

    @LayoutConstructor
    public AddMenuItem(UUID restaurantId, String name, String description, String pictureContentType, byte[] picture,
                       BigDecimal price) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.pictureContentType = pictureContentType;
        this.picture = picture;
        this.price = price;
    }

    public AddMenuItem(Restaurant restaurant, String name, String description, String pictureContentType, byte[] picture,
                       BigDecimal price) {
        this(restaurant.getId(), name, description, pictureContentType, picture, price);
    }


    @Override public EventStream<MenuItemAdded> events() throws Exception {
        MenuItemAdded menuItemAdded = new MenuItemAdded(restaurantId);
        NameChanged nameChanged = new NameChanged(menuItemAdded.uuid(), name);
        DescriptionChanged descriptionChanged = new DescriptionChanged(menuItemAdded.uuid(), description);
        PictureChanged pictureChanged = new PictureChanged(menuItemAdded.uuid(), pictureContentType, picture);
        PriceChanged priceChanged = new PriceChanged(menuItemAdded.uuid(), price);
        return EventStream.ofWithState(menuItemAdded, menuItemAdded, nameChanged, descriptionChanged,
                                       pictureChanged, priceChanged);
    }

    @Override public MenuItem result(MenuItemAdded state, Repository repository) {
        return MenuItem.lookup(repository, state.uuid()).get();
    }
}
