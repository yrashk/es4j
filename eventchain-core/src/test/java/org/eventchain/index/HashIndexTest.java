/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain.index;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.support.KeyStatistics;
import com.googlecode.cqengine.index.support.KeyStatisticsIndex;
import com.googlecode.cqengine.resultset.ResultSet;
import org.eventchain.models.Car;
import org.eventchain.models.CarFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// This test was originally copied from CQEngine in order to test
// indices the same way CQEngine does.
public abstract class HashIndexTest<HashIndex extends AttributeIndex> {

    public abstract <A, O> HashIndex onAttribute(Attribute<O, A> attribute);

    public static <A> Set<A> setOf(A... values) {
        return new LinkedHashSet<A>(Arrays.asList(values));
    }

    public static <A> Set<A> setOf(Iterable<A> values) {
        Set<A> result = new LinkedHashSet<A>();
        for (A value : values) {
            result.add(value);
        }
        return result;
    }

    @Test
    public void getDistinctKeysAndCounts() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MODEL_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(Car.MODEL);
        collection.addIndex(MODEL_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<String> distinctModels = setOf(MODEL_INDEX.getDistinctKeys(noQueryOptions()));
        assertEquals(distinctModels, setOf("Accord", "Avensis", "Civic", "Focus", "Fusion", "Hilux", "Insight", "M6", "Prius", "Taurus"));
        for (String model : distinctModels) {
            assertEquals(MODEL_INDEX.getCountForKey(model, noQueryOptions()), Integer.valueOf(2));
        }
    }

    @Test
    public void getCountOfDistinctKeys(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        assertEquals(MANUFACTURER_INDEX.getCountOfDistinctKeys(noQueryOptions()), Integer.valueOf(4));
    }

    @Test
    public void getStatisticsForDistinctKeys(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(MANUFACTURER_INDEX.getStatisticsForDistinctKeys(noQueryOptions()));
        assertEquals(keyStatistics, setOf(
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("BMW", 2)
                )
                );
    }

    @Test
    public void retrieveEqual() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(10));

        ResultSet<Car> cars = collection.retrieve(equal(Car.MANUFACTURER, "Honda"));
        assertTrue(cars.isNotEmpty());
        assertTrue(StreamSupport.stream(cars.spliterator(), false).allMatch(car -> car.getManufacturer().contentEquals("Honda")));
    }

    @Test
    public void retrieveHas() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = (KeyStatisticsIndex<String, Car>) onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        Set<Car> coll = CarFactory.createCollectionOfCars(10);
        collection.addAll(coll);

        ResultSet<Car> cars = collection.retrieve(has(Car.MANUFACTURER));
        assertTrue(cars.isNotEmpty());
        assertEquals(cars.size(), coll.size());
    }
}
