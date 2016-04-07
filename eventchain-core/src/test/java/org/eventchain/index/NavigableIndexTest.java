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
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.support.KeyStatistics;
import com.googlecode.cqengine.index.support.KeyStatisticsIndex;
import com.googlecode.cqengine.index.support.SortedKeyStatisticsIndex;
import com.googlecode.cqengine.quantizer.IntegerQuantizer;
import com.googlecode.cqengine.quantizer.Quantizer;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import org.eventchain.models.Car;
import org.eventchain.models.CarFactory;
import org.testng.annotations.Test;

import java.util.*;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

// This test was originally copied from CQEngine in order to test
// indices the same way CQEngine does.
public abstract class NavigableIndexTest<NavigableIndex extends AttributeIndex & SortedKeyStatisticsIndex> {

    public abstract <A extends Comparable<A>, O> NavigableIndex onAttribute(Attribute<O, A> attribute);
    public abstract <A extends Comparable<A>, O> Index<O> withQuantizerOnAttribute(Quantizer<A> quantizer, Attribute<O, A> attribute);

    public static <A> Set<A> setOf(A... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    public static <A> Set<A> setOf(Iterable<A> values) {
        Set<A> result = new LinkedHashSet<>();
        for (A value : values) {
            result.add(value);
        }
        return result;
    }

    @Test
    public void getDistinctKeysAndCounts() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, Car> MODEL_INDEX = onAttribute(Car.MODEL);
        collection.addIndex(MODEL_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<String> distinctModels = setOf(MODEL_INDEX.getDistinctKeys(noQueryOptions()));
        assertEquals(new ArrayList<>(distinctModels), asList("Accord", "Avensis", "Civic", "Focus", "Fusion", "Hilux", "Insight", "M6", "Prius", "Taurus"));
        for (String model : distinctModels) {
            assertEquals(Integer.valueOf(2), MODEL_INDEX.getCountForKey(model, noQueryOptions()));
        }

        Set<String> distinctModelsDescending = setOf(MODEL_INDEX.getDistinctKeysDescending(noQueryOptions()));
        assertEquals(new ArrayList<>(distinctModelsDescending), asList("Taurus", "Prius", "M6", "Insight", "Hilux", "Fusion", "Focus", "Civic", "Avensis", "Accord"));
    }

    @Test
    public void getCountOfDistinctKeys(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        assertEquals(MANUFACTURER_INDEX.getCountOfDistinctKeys(noQueryOptions()), Integer.valueOf(4));
    }

    @Test
    public void getStatisticsForDistinctKeys(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(MANUFACTURER_INDEX.getStatisticsForDistinctKeys(noQueryOptions()));
        assertEquals(keyStatistics, setOf(
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("BMW", 2)

        ));
    }

    @Test
    public void getStatisticsForDistinctKeysDescending(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(MANUFACTURER_INDEX.getStatisticsForDistinctKeysDescending(noQueryOptions()));
        assertEquals(keyStatistics, setOf(
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("BMW", 2)

        ));
    }

    @Test
    public void indexQuantization_SpanningTwoBucketsMidRange() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 20 because this query spans 2 buckets (each containing 10 objects)...
        assertEquals(collection.retrieve(between(Car.CAR_ID, 47, 53)).getMergeCost(), 20);

        // 7 objects match the query (between is inclusive)...
        assertEquals(collection.retrieve(between(Car.CAR_ID, 47, 53)).size(), 7);

        // The matching objects are...
        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 47, 53));
        assertEquals(carIdsFound, asList(47, 48, 49, 50, 51, 52, 53));
    }

    @Test
    public void indexQuantization_FirstBucket() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        assertEquals(collection.retrieve(between(Car.CAR_ID, 2, 4)).getMergeCost(), 10);

        // 3 objects match the query...
        assertEquals(collection.retrieve(between(Car.CAR_ID, 2, 4)).size(), 3);

        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 2, 4));
        assertEquals(carIdsFound, asList(2, 3, 4));
    }

    @Test
    public void indexQuantization_LastBucket() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        assertEquals(collection.retrieve(between(Car.CAR_ID, 96, 98)).getMergeCost(), 10);

        // 3 objects match the query...
        assertEquals(collection.retrieve(between(Car.CAR_ID, 96, 98)).size(), 3);

        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 96, 98));
        assertEquals(carIdsFound, asList(96, 97, 98));
    }


    @Test
    public void indexQuantization_LessThan() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        assertEquals(collection.retrieve(lessThan(Car.CAR_ID, 5)).size(), 5);
        assertEquals(collection.retrieve(lessThan(Car.CAR_ID, 15)).size(), 15);
        assertEquals(collection.retrieve(lessThanOrEqualTo(Car.CAR_ID, 5)).size(), 6);
        assertEquals(collection.retrieve(lessThanOrEqualTo(Car.CAR_ID, 15)).size(), 16);
    }

    @Test
    public void indexQuantization_GreaterThan() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        assertEquals(collection.retrieve(greaterThan(Car.CAR_ID, 95)).size(), 4);
        assertEquals(collection.retrieve(greaterThan(Car.CAR_ID, 85)).size(), 14);
        assertEquals(collection.retrieve(greaterThanOrEqualTo(Car.CAR_ID, 95)).size(), 5);
        assertEquals(collection.retrieve(greaterThanOrEqualTo(Car.CAR_ID, 85)).size(), 15);
    }

    @Test
    public void indexQuantization_Between() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        Query<Car> query = between(Car.CAR_ID, 88, 92);
        assertEquals(collection.retrieve(query).size(), 5);
        assertEquals(retrieveCarIds(collection, query), asList(88, 89, 90, 91, 92));

        query = between(Car.CAR_ID, 88, true, 92, true);
        assertEquals(collection.retrieve(query).size(), 5);
        assertEquals(retrieveCarIds(collection, query), asList(88, 89, 90, 91, 92));

        query = between(Car.CAR_ID, 88, false, 92, true);
        assertEquals(collection.retrieve(query).size(), 4);
        assertEquals(retrieveCarIds(collection, query), asList(89, 90, 91, 92));

        query = between(Car.CAR_ID, 88, true, 92, false);
        assertEquals(collection.retrieve(query).size(), 4);
        assertEquals(retrieveCarIds(collection, query), asList(88, 89, 90, 91));

        query = between(Car.CAR_ID, 88, false, 92, false);
        assertEquals(collection.retrieve(query).size(), 3);
        assertEquals(retrieveCarIds(collection, query), asList(89, 90, 91));
    }

    @Test
    public void indexQuantization_ComplexQuery() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));
        Query<Car> query = and(between(Car.CAR_ID, 96, 98), greaterThan(Car.CAR_ID, 95));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        assertEquals(collection.retrieve(query).getMergeCost(), 10);

        // 3 objects match the query...
        assertEquals(collection.retrieve(query).size(), 3);

        List<Integer> carIdsFound = retrieveCarIds(collection, query);
        assertEquals(carIdsFound, asList(96, 97, 98));
    }

    static List<Integer> retrieveCarIds(IndexedCollection<Car> collection, Query<Car> query) {
        ResultSet<Car> cars = collection.retrieve(query, queryOptions(orderBy(ascending(Car.CAR_ID))));
        List<Integer> carIds = new ArrayList<>();
        for (Car car : cars) {
            carIds.add(car.getCarId());
        }
        return carIds;
    }


}
