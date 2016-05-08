/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Getter;
import lombok.Setter;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import static org.testng.Assert.assertEquals;

// This test was originally copied from CQEngine in order to test
// indices the same way CQEngine does.
public abstract class UniqueIndexTest<UniqueIndex extends AttributeIndex> {

    public abstract <A, O> UniqueIndex onAttribute(Attribute<O, A> attribute);

    public static class Car {
        @Getter @Setter
        public int carId;
        @Getter @Setter
        public String name;
        @Getter @Setter
        public String description;
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
        public static final Attribute<Car, Integer> CAR_ID = new com.googlecode.cqengine.attribute.SimpleAttribute<Car, Integer>("carId") {
            public Integer getValue(Car car, QueryOptions queryOptions) { return car.carId; }
        };

        public static final Attribute<Car, String> NAME = new com.googlecode.cqengine.attribute.SimpleAttribute<Car, String>("name") {
            public String getValue(Car car, QueryOptions queryOptions) { return car.name; }
        };

        public static final Attribute<Car, String> DESCRIPTION = new com.googlecode.cqengine.attribute.SimpleAttribute<Car, String>("description") {
            public String getValue(Car car, QueryOptions queryOptions) { return car.description; }
        };

        public static final Attribute<Car, String> FEATURES = new MultiValueAttribute<Car, String>("features") {
            public List<String> getValues(Car car, QueryOptions queryOptions) { return car.features; }
        };
    }

    @Test
    public void uniqueIndex() {
        IndexedCollection<Car> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        UniqueIndex index = onAttribute(Car.CAR_ID);
        cars.addIndex(index);
        cars.addIndex(HashIndex.onAttribute(Car.CAR_ID));

        // Add some objects to the collection...
        cars.add(new Car(1, "ford focus", "great condition, low mileage", Arrays.asList("spare tyre", "sunroof")));
        cars.add(new Car(2, "ford taurus", "dirty and unreliable, flat tyre", Arrays.asList("spare tyre", "radio")));
        cars.add(new Car(3, "honda civic", "has a flat tyre and high mileage", Arrays.asList("radio")));

        Query<Car> query = equal(Car.CAR_ID, 2);
        ResultSet<Car> rs = cars.retrieve(query);
        assertEquals(rs.getRetrievalCost(), index.retrieve(query, noQueryOptions()).getRetrievalCost(), "should prefer unique index over hash index");

        assertEquals(rs.uniqueResult().carId, 2, "should retrieve car 2");
    }

    @Test(expectedExceptions = com.googlecode.cqengine.index.unique.UniqueIndex.UniqueConstraintViolatedException.class)
    public void duplicateObjectDetection_SimpleAttribute() {
        IndexedCollection<Car> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        cars.addIndex(onAttribute(Car.CAR_ID));

        // Add some objects to the collection...
        cars.add(new Car(1, "ford focus", "great condition, low mileage", Arrays.asList("spare tyre", "sunroof")));
        cars.add(new Car(2, "ford taurus", "dirty and unreliable, flat tyre", Arrays.asList("spare tyre", "radio")));
        cars.add(new Car(3, "honda civic", "has a flat tyre and high mileage", Arrays.asList("radio")));

        cars.add(new Car(2, "some other car", "foo", Arrays.asList("bar")));
    }

    @Test(expectedExceptions = com.googlecode.cqengine.index.unique.UniqueIndex.UniqueConstraintViolatedException.class)
    public void duplicateObjectDetection_MultiValueAttribute() {
        IndexedCollection<Car> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        cars.addIndex(onAttribute(Car.FEATURES));

        // Add some objects to the collection...
        cars.add(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof")));
        cars.add(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player")));

        // Try to add another car which has a cd player, when one car already has a cd player...
        cars.add(new Car(3, "honda civic", "baz", Arrays.asList("cd player", "bluetooth")));
    }

    @Test
    public void retrieve() {
        IndexedCollection<Car> cars = new ConcurrentIndexedCollection<>();

        // Add some indexes...
        cars.addIndex(onAttribute(Car.FEATURES));

        // Add some objects to the collection...
        cars.add(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof")));
        cars.add(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player")));

        assertEquals(cars.retrieve(equal(Car.FEATURES, "radio")).size(), 1);
        assertEquals(cars.retrieve(equal(Car.FEATURES, "unknown")).size(), 0);
    }

    @Test
    public void indexingExistingData() {
        IndexedCollection<Car> cars = new ConcurrentIndexedCollection<>();

        // Add some objects to the collection...
        cars.add(new Car(1, "ford focus", "foo", Arrays.asList("spare tyre", "sunroof")));
        cars.add(new Car(2, "ford taurus", "bar", Arrays.asList("radio", "cd player")));

        // Add some indexes...
        cars.addIndex(onAttribute(Car.FEATURES));

        assertEquals(cars.retrieve(equal(Car.FEATURES, "radio")).size(), 1);
        assertEquals(cars.retrieve(equal(Car.FEATURES, "unknown")).size(), 0);
    }

}
