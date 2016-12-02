/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Model;
import com.eventsourcing.Repository;
import com.eventsourcing.cep.protocols.DeletedProtocol;
import com.eventsourcing.cep.protocols.DescriptionProtocol;
import com.eventsourcing.cep.protocols.NameProtocol;
import com.eventsourcing.queries.ModelCollectionQuery;
import com.eventsourcing.queries.ModelQueries;
import com.googlecode.cqengine.resultset.ResultSet;
import foodsourcing.events.MenuItemAdded;
import foodsourcing.protocols.PictureProtocol;
import foodsourcing.protocols.PriceProtocol;
import lombok.Getter;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.eventsourcing.index.EntityQueryFactory.*;
import static com.googlecode.cqengine.stream.StreamFactory.streamOf;

public class MenuItem
        implements Model, NameProtocol, DescriptionProtocol, PriceProtocol, PictureProtocol,
        DeletedProtocol {
    @Getter
    private final Repository repository;
    @Getter
    private final UUID id;


    protected MenuItem(Repository repository, UUID id) {
        this.repository = repository;
        this.id = id;
    }

    public static Optional<MenuItem> lookup(Repository repository, UUID id) {
        Optional<MenuItemAdded> restaurantRegistered =
                ModelQueries.lookup(repository, MenuItemAdded.class, MenuItemAdded.ID, id);
        if (restaurantRegistered.isPresent()) {
            return Optional.of(new MenuItem(repository, id));
        } else {
            return Optional.empty();
        }
    }

    public static Collection<MenuItem> query(Repository repository, ModelCollectionQuery<MenuItem> query) {
        return ModelCollectionQuery.query(repository, query);
    }

    public static class BelongsToRestaurant implements ModelCollectionQuery<MenuItem> {

        private final UUID restaurantId;

        public BelongsToRestaurant(UUID restaurantId) {this.restaurantId = restaurantId;}

        @Override public Stream<MenuItem> getCollectionStream(Repository repository) {
            ResultSet<EntityHandle<MenuItemAdded>> resultSet =
                    repository.query(MenuItemAdded.class, equal(MenuItemAdded.REFERENCE_ID, restaurantId));
            return streamOf(resultSet)
                    .map(h -> new MenuItem(repository, h.uuid()))
                    .onClose(resultSet::close);
        }
    }

    public static ModelCollectionQuery<MenuItem> belongsToRestaurant(Restaurant restaurant) {
        return new BelongsToRestaurant(restaurant.getId());
    }
}
