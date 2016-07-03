/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.models.Car;
import com.eventsourcing.models.CarFactory;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.support.CloseableIterable;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.index.support.KeyStatistics;
import com.googlecode.cqengine.index.support.KeyStatisticsIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.resultset.ResultSet;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// This test was originally copied from CQEngine in order to test
// indices the same way CQEngine does.
public abstract class EqualityIndexTest<HashIndex extends AttributeIndex> {

    public abstract <A, O extends Entity> HashIndex onAttribute(Attribute<O, A> attribute);

    public static <A> Set<A> setOf(A... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    public static <A> Set<A> setOf(CloseableIterable<A> values) {
        Set<A> result = new LinkedHashSet<>();
        CloseableIterator<A> iterator = values.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    @Test
    public void getDistinctKeysAndCounts() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MODEL_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(Car.MODEL);
        MODEL_INDEX.clear(noQueryOptions());

        collection.addIndex(MODEL_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<String> distinctModels = setOf(MODEL_INDEX.getDistinctKeys(noQueryOptions()));
        assertEquals(distinctModels,
                     setOf("Accord", "Avensis", "Civic", "Focus", "Fusion", "Hilux", "Insight", "M6", "Prius",
                           "Taurus"));
        for (String model : distinctModels) {
            assertEquals(MODEL_INDEX.getCountForKey(model, noQueryOptions()), Integer.valueOf(2));
        }

        MODEL_INDEX.clear(noQueryOptions());
    }

    @Test
    public void getCountOfDistinctKeys() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(
                Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());

        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        assertEquals(MANUFACTURER_INDEX.getCountOfDistinctKeys(noQueryOptions()), Integer.valueOf(4));

        MANUFACTURER_INDEX.clear(noQueryOptions());
    }

    @Test
    public void getStatisticsForDistinctKeys() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(
                Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());

        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(
                MANUFACTURER_INDEX.getStatisticsForDistinctKeys(noQueryOptions()));
        assertEquals(keyStatistics, setOf(
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("BMW", 2)
                     )
        );
        MANUFACTURER_INDEX.clear(noQueryOptions());
    }

    @Test
    public void retrieveEqual() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(
                Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());

        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(10));

        ResultSet<Car> cars = collection.retrieve(equal(Car.MANUFACTURER, "Honda"));
        assertTrue(cars.isNotEmpty());
        assertTrue(StreamSupport.stream(cars.spliterator(), false)
                                .allMatch(car -> car.getManufacturer().contentEquals("Honda")));

        cars.close();
        MANUFACTURER_INDEX.clear(noQueryOptions());
    }

    @Test
    public void retrieveHas() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(
                Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());
        collection.addIndex(MANUFACTURER_INDEX);

        Set<Car> coll = CarFactory.createCollectionOfCars(10);
        collection.addAll(coll);

        ResultSet<Car> cars = collection.retrieve(has(Car.MANUFACTURER));
        assertTrue(cars.isNotEmpty());
        assertEquals(cars.size(), coll.size());

        cars.close();
        MANUFACTURER_INDEX.clear(noQueryOptions());
    }

    @Test
    public void indexExistingData() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(
                Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());

        collection.addAll(CarFactory.createCollectionOfCars(10));

        collection.addIndex(MANUFACTURER_INDEX);

        ResultSet<Car> cars = collection.retrieve(equal(Car.MANUFACTURER, "Honda"));
        assertTrue(cars.isNotEmpty());
        assertTrue(StreamSupport.stream(cars.spliterator(), false)
                                .allMatch(car -> car.getManufacturer().contentEquals("Honda")));

        cars.close();
        MANUFACTURER_INDEX.clear(noQueryOptions());
    }
}
