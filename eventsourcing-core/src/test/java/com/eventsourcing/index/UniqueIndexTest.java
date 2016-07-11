/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.StandardEntity;
import com.eventsourcing.ResolvedEntityHandle;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Getter;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import static org.testng.Assert.assertEquals;

// This test was originally copied from CQEngine in order to test
// indices the same way CQEngine does.
public abstract class UniqueIndexTest<UniqueIndex extends AttributeIndex> {

    public abstract <A, O extends Entity> UniqueIndex onAttribute(Attribute<O, A> attribute);

    public static class Car extends StandardEntity {
        @Getter
        public int carId;
        @Getter
        public String name;
        @Getter
        public String description;
        @Getter
        public List<String> features;

        public Car() {}

        public Car(int carId, String name, String description, List<String> features) {
            this.carId = carId;
            this.name = name;
            this.description = description;
            this.features = features;
        }

        @Override
        public String toString() {
            return "Car{carId=" + carId + ", name='" + name + "', description='" + description + "', features=" + features + "}";
        }

        // -------------------------- Attributes --------------------------
        public static final Attribute<Car, Integer> CAR_ID = new SimpleAttribute<Car, Integer>(
                "carId") {
            public Integer getValue(Car car, QueryOptions queryOptions) { return car.carId; }
        };

        public static final Attribute<Car, String> NAME = new SimpleAttribute<Car, String>(
                "name") {
            public String getValue(Car car, QueryOptions queryOptions) { return car.name; }
        };

        public static final Attribute<Car, String> DESCRIPTION = new SimpleAttribute<Car, String>(
                "description") {
            public String getValue(Car car, QueryOptions queryOptions) { return car.description; }
        };

        public static final Attribute<Car, String> FEATURES = new MultiValueAttribute<Car, String>("features") {
            public List<String> getValues(Car car, QueryOptions queryOptions) { return car.features; }
        };
    }

    @Test
    public void uniqueIndex() {
        IndexedCollection<EntityHandle<Car>> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        UniqueIndex index = onAttribute(Car.CAR_ID);
        cars.addIndex(index);
        HashIndex<Integer, EntityHandle<Car>> index1 = HashIndex.onAttribute(Car.CAR_ID);
        cars.addIndex(index1);
        index.clear(noQueryOptions());
        index1.clear(noQueryOptions());

        // Add some objects to the collection...
        cars.add(new ResolvedEntityHandle<>(new Car(1, "ford focus", "great condition, low mileage", Arrays.asList("spare tyre",
                                                                                              "sunroof"))));
        cars.add(new ResolvedEntityHandle<>(new Car(2, "ford taurus", "dirty and unreliable, flat tyre", Arrays.asList("spare tyre",
                                                                                                "radio"))));
        cars.add(new ResolvedEntityHandle<>(new Car(3, "honda civic", "has a flat tyre and high mileage", Arrays
                .asList("radio"))));

        Query<EntityHandle<Car>> query = equal(Car.CAR_ID, 2);
        ResultSet<EntityHandle<Car>> rs = cars.retrieve(query);
        assertEquals(rs.getRetrievalCost(), index.retrieve(query, noQueryOptions()).getRetrievalCost(),
                     "should prefer unique index over hash index");

        assertEquals(rs.uniqueResult().get().carId, 2, "should retrieve car 2");

        index.clear(noQueryOptions());
        index1.clear(noQueryOptions());

    }

    @Test(expectedExceptions = com.googlecode.cqengine.index.unique.UniqueIndex.UniqueConstraintViolatedException.class)
    public void duplicateObjectDetection_SimpleAttribute() {
        IndexedCollection<EntityHandle<Car>> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        UniqueIndex index = onAttribute(Car.CAR_ID);
        cars.addIndex(index);
        index.clear(noQueryOptions());

        // Add some objects to the collection...
        cars.add(new ResolvedEntityHandle<>(new Car(1, "ford focus", "great condition, low mileage", Arrays.asList("spare tyre",
                                                                                              "sunroof"))));
        cars.add(new ResolvedEntityHandle<>(new Car(2, "ford taurus", "dirty and unreliable, flat tyre", Arrays.asList("spare tyre",
                                                                                                "radio"))));
        cars.add(new ResolvedEntityHandle<>(new Car(3, "honda civic", "has a flat tyre and high mileage", Arrays
                .asList("radio"))));

        cars.add(new ResolvedEntityHandle<>(new Car(2, "some other car", "foo", Arrays.asList("bar"))));
        index.clear(noQueryOptions());
    }

    @Test(expectedExceptions = com.googlecode.cqengine.index.unique.UniqueIndex.UniqueConstraintViolatedException.class)
    public void duplicateObjectDetection_MultiValueAttribute() {
        IndexedCollection<EntityHandle<Car>> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        UniqueIndex index = onAttribute(Car.FEATURES);
        cars.addIndex(index);
        index.clear(noQueryOptions());

        // Add some objects to the collection...
        cars.add(new ResolvedEntityHandle<>(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof"))));
        cars.add(new ResolvedEntityHandle<>(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player"))));

        // Try to add another car which has a cd player, when one car already has a cd player...
        cars.add(new ResolvedEntityHandle<>(new Car(3, "honda civic", "baz", Arrays.asList("cd player", "bluetooth"))));
        index.clear(noQueryOptions());
    }

    @Test
    public void retrieve() {
        IndexedCollection<EntityHandle<Car>> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        UniqueIndex index = onAttribute(Car.FEATURES);
        cars.addIndex(index);
        index.clear(noQueryOptions());

        // Add some objects to the collection...
        cars.add(new ResolvedEntityHandle<>(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof"))));
        cars.add(new ResolvedEntityHandle<>(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player"))));

        ResultSet<EntityHandle<Car>> radio = cars.retrieve(equal(Car.FEATURES, "radio"));
        assertEquals(radio.size(), 1);
        radio.close();
        ResultSet<EntityHandle<Car>> unknown = cars.retrieve(equal(Car.FEATURES, "unknown"));
        assertEquals(unknown.size(), 0);
        unknown.close();
        index.clear(noQueryOptions());
    }

    @Test
    public void indexingExistingData() {
        IndexedCollection<EntityHandle<Car>> cars = new ConcurrentIndexedCollection<>();
        UniqueIndex index = onAttribute(Car.FEATURES);
        index.clear(noQueryOptions());

        // Add some objects to the collection...
        cars.add(new ResolvedEntityHandle<>(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof"))));
        cars.add(new ResolvedEntityHandle<>(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player"))));

        // Add some indexes...
        cars.addIndex(index);

        ResultSet<EntityHandle<Car>> radio = cars.retrieve(equal(Car.FEATURES, "radio"));
        assertEquals(radio.size(), 1);
        radio.close();
        ResultSet<EntityHandle<Car>> unknown = cars.retrieve(equal(Car.FEATURES, "unknown"));
        assertEquals(unknown.size(), 0);
        unknown.close();
        index.clear(noQueryOptions());
    }

    @Test
    public void reindexData() {
        IndexedCollection<EntityHandle<Car>> cars = new ConcurrentIndexedCollection<>();
        UniqueIndex index = onAttribute(Car.FEATURES);
        index.clear(noQueryOptions());

        // Add some objects to the collection...
        cars.add(new ResolvedEntityHandle<>(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof"))));
        cars.add(new ResolvedEntityHandle<>(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player"))));


        cars.addIndex(index);

        IndexedCollection<EntityHandle<Car>> cars1 = new ConcurrentIndexedCollection<>();
        UniqueIndex index1 = onAttribute(Car.FEATURES);
        cars1.addAll(cars);

        cars1.addIndex(index1);

        ResultSet<EntityHandle<Car>> radio = cars.retrieve(equal(Car.FEATURES, "radio"));
        assertEquals(radio.size(), 1);
        radio.close();

        index.clear(noQueryOptions());
        index1.clear(noQueryOptions());
    }

}
