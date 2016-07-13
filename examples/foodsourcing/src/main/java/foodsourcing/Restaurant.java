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
import com.eventsourcing.cep.protocols.NameProtocol;
import com.eventsourcing.queries.ModelCollectionQuery;
import com.eventsourcing.queries.ModelQueries;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.logical.And;
import com.googlecode.cqengine.resultset.ResultSet;
import foodsourcing.events.AddressChanged;
import foodsourcing.events.MenuItemAdded;
import foodsourcing.events.RestaurantRegistered;
import foodsourcing.events.WorkingHoursChanged;
import foodsourcing.protocols.AddressProtocol;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.eventsourcing.cep.protocols.DeletedProtocol.notDeleted;
import static com.eventsourcing.queries.QueryFactory.isLatestEntity;
import static com.eventsourcing.index.EntityQueryFactory.*;
import static com.eventsourcing.queries.ModelCollectionQuery.LogicalOperators.*;

public class Restaurant implements Model, NameProtocol, AddressProtocol {
    @Getter
    private final Repository repository;
    @Getter
    private final UUID id;

    @Override public boolean equals(Object obj) {
        return obj instanceof Restaurant && getId().equals(((Restaurant) obj).getId());
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    protected Restaurant(Repository repository, UUID id) {
        this.repository = repository;
        this.id = id;
    }

    @Override public String toString() {
        return "Restaurant(name=" + name() + ")";
    }

    public static Optional<Restaurant> lookup(Repository repository, UUID id) {
        Optional<RestaurantRegistered> restaurantRegistered =
                ModelQueries.lookup(repository, RestaurantRegistered.class, RestaurantRegistered.ID, id);
        if (restaurantRegistered.isPresent()) {
            return Optional.of(new Restaurant(repository, id));
        } else {
            return Optional.empty();
        }
    }

    private static class Within10Km implements ModelCollectionQuery<Restaurant> {

        private final Address address;

        public Within10Km(Address address) {
            this.address = address;
        }

        @Override public Stream<Restaurant> getCollectionStream(Repository repository) {
            And<EntityHandle<AddressChanged>> geoRestrictions = and(
                    lessThanOrEqualTo(AddressChanged.BOUNDING_BOX_10K_LAT_START, address.latitude()),
                    lessThanOrEqualTo(AddressChanged.BOUNDING_BOX_10K_LONG_START, address.longitude()),
                    greaterThanOrEqualTo(AddressChanged.BOUNDING_BOX_10K_LAT_END, address.latitude()),
                    greaterThanOrEqualTo(AddressChanged.BOUNDING_BOX_10K_LONG_END, address.longitude()));
            Query<EntityHandle<AddressChanged>> isRestaurant = existsIn(
                    repository.getIndexEngine().getIndexedCollection(RestaurantRegistered.class),
                    AddressChanged.REFERENCE_ID, RestaurantRegistered.ID);
            Query<EntityHandle<AddressChanged>> latestEntity =
                    isLatestEntity(repository.getIndexEngine().getIndexedCollection(AddressChanged.class),
                                   (v) -> equal(AddressChanged.REFERENCE_ID, v.get().reference()),
                                   AddressChanged.TIMESTAMP);
            Query<EntityHandle<AddressChanged>> query = and(latestEntity, geoRestrictions, isRestaurant);
            ResultSet<EntityHandle<AddressChanged>> resultSet = repository.query(AddressChanged.class, query);
            return StreamSupport
                    .stream(resultSet.spliterator(), false)
                    .map(h -> new Restaurant(repository, h.get().reference()))
                    .onClose(resultSet::close);
        }
    }

    public static ModelCollectionQuery<Restaurant> within10km(Address address) {
       return new Within10Km(address);
    }

    private static class OpenAt implements ModelCollectionQuery<Restaurant> {

        private final WorkingHoursChanged.OpeningHoursBoundary boundary;

        public OpenAt(Date date) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            OpeningHours.Time time = new OpeningHours.Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            boundary = new WorkingHoursChanged.OpeningHoursBoundary(DayOfWeek.of(dayOfWeek), time);
        }

        @Override public Stream<Restaurant> getCollectionStream(Repository repository) {
            Query<EntityHandle<WorkingHoursChanged>> latestEntity =
                    isLatestEntity(repository.getIndexEngine().getIndexedCollection(WorkingHoursChanged.class),
                                   (v) -> and(
                                           lessThanOrEqualTo(WorkingHoursChanged.OPENING_AT, boundary),
                                           greaterThanOrEqualTo(WorkingHoursChanged.CLOSING_AT, boundary),
                                           equal(WorkingHoursChanged.REFERENCE_ID, v.get().reference())),
                                   WorkingHoursChanged.TIMESTAMP);
            Query<EntityHandle<WorkingHoursChanged>> isRestaurant = existsIn(
                    repository.getIndexEngine().getIndexedCollection(RestaurantRegistered.class),
                    WorkingHoursChanged.REFERENCE_ID, RestaurantRegistered.ID);
            Query<EntityHandle<WorkingHoursChanged>> query = and(latestEntity, isRestaurant,lessThanOrEqualTo(WorkingHoursChanged.OPENING_AT, boundary),
                                                                 greaterThanOrEqualTo(WorkingHoursChanged.CLOSING_AT, boundary));
            ResultSet<EntityHandle<WorkingHoursChanged>> resultSet = repository.query(WorkingHoursChanged.class, query);
            return StreamSupport
                    .stream(resultSet.spliterator(), false)
                    .map(h -> new Restaurant(repository, h.get().reference()))
                    .onClose(resultSet::close);
        }
    }

    public static ModelCollectionQuery<Restaurant> openAt(Date date) {
        return new OpenAt(date);
    }


    public static Collection<Restaurant> query(Repository repository, ModelCollectionQuery<Restaurant> query) {
        return ModelCollectionQuery.query(repository, query);
    }

    public Map<Integer, List<OpeningHours>> openingHours() {
        IndexedCollection<EntityHandle<WorkingHoursChanged>> collection =
                getRepository().getIndexEngine().getIndexedCollection(WorkingHoursChanged.class);
        Query<EntityHandle<WorkingHoursChanged>> matchingReference = equal(WorkingHoursChanged.REFERENCE_ID, getId());
        Query<EntityHandle<WorkingHoursChanged>> query =
                and(matchingReference, isLatestEntity(collection,
                                                      h -> and(matchingReference,
                                                               equal(WorkingHoursChanged.DAY_OF_WEEK, h.get().dayOfWeek())),
                                                      WorkingHoursChanged.TIMESTAMP));
        try (ResultSet<EntityHandle<WorkingHoursChanged>> resultSet =
                     getRepository().query(WorkingHoursChanged.class, query)) {
            return
            StreamSupport.stream(resultSet.spliterator(), false)
                         .map(EntityHandle::get)
                         .collect(Collectors.toMap(e -> e.dayOfWeek().getValue(), WorkingHoursChanged::openDuring));
        }
    }

    public Collection<MenuItem> menu() {
        ModelCollectionQuery<MenuItem> query = and(MenuItem.belongsToRestaurant(this),
                                                  notDeleted(MenuItemAdded.class, MenuItemAdded.ID,
                                                             MenuItem::lookup));
        return MenuItem.query(getRepository(), query);
    }
}
