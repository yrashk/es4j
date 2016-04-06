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
        assertEquals(asList("Accord", "Avensis", "Civic", "Focus", "Fusion", "Hilux", "Insight", "M6", "Prius", "Taurus"), new ArrayList<>(distinctModels));
        for (String model : distinctModels) {
            assertEquals(Integer.valueOf(2), MODEL_INDEX.getCountForKey(model, noQueryOptions()));
        }

        Set<String> distinctModelsDescending = setOf(MODEL_INDEX.getDistinctKeysDescending(noQueryOptions()));
        assertEquals(asList("Taurus", "Prius", "M6", "Insight", "Hilux", "Fusion", "Focus", "Civic", "Avensis", "Accord"), new ArrayList<>(distinctModelsDescending));
    }

    @Test
    public void getCountOfDistinctKeys(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        assertEquals(Integer.valueOf(4), MANUFACTURER_INDEX.getCountOfDistinctKeys(noQueryOptions()));
    }

    @Test
    public void getStatisticsForDistinctKeys(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(MANUFACTURER_INDEX.getStatisticsForDistinctKeys(noQueryOptions()));
        assertEquals(setOf(
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("BMW", 2)

                ),
                keyStatistics);
    }

    @Test
    public void getStatisticsForDistinctKeysDescending(){
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, Car> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(MANUFACTURER_INDEX.getStatisticsForDistinctKeysDescending(noQueryOptions()));
        assertEquals(setOf(
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("BMW", 2)

                ),
                keyStatistics);
    }

    @Test
    public void indexQuantization_SpanningTwoBucketsMidRange() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 20 because this query spans 2 buckets (each containing 10 objects)...
        assertEquals(20, collection.retrieve(between(Car.CAR_ID, 47, 53)).getMergeCost());

        // 7 objects match the query (between is inclusive)...
        assertEquals(7, collection.retrieve(between(Car.CAR_ID, 47, 53)).size());

        // The matching objects are...
        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 47, 53));
        assertEquals(asList(47, 48, 49, 50, 51, 52, 53), carIdsFound);
    }

    @Test
    public void indexQuantization_FirstBucket() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        assertEquals(10, collection.retrieve(between(Car.CAR_ID, 2, 4)).getMergeCost());

        // 3 objects match the query...
        assertEquals(3, collection.retrieve(between(Car.CAR_ID, 2, 4)).size());

        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 2, 4));
        assertEquals(asList(2, 3, 4), carIdsFound);
    }

    @Test
    public void indexQuantization_LastBucket() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        assertEquals(10, collection.retrieve(between(Car.CAR_ID, 96, 98)).getMergeCost());

        // 3 objects match the query...
        assertEquals(3, collection.retrieve(between(Car.CAR_ID, 96, 98)).size());

        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 96, 98));
        assertEquals(asList(96, 97, 98), carIdsFound);
    }


    @Test
    public void indexQuantization_LessThan() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        assertEquals(5, collection.retrieve(lessThan(Car.CAR_ID, 5)).size());
        assertEquals(15, collection.retrieve(lessThan(Car.CAR_ID, 15)).size());
        assertEquals(6, collection.retrieve(lessThanOrEqualTo(Car.CAR_ID, 5)).size());
        assertEquals(16, collection.retrieve(lessThanOrEqualTo(Car.CAR_ID, 15)).size());
    }

    @Test
    public void indexQuantization_GreaterThan() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        assertEquals(4, collection.retrieve(greaterThan(Car.CAR_ID, 95)).size());
        assertEquals(14, collection.retrieve(greaterThan(Car.CAR_ID, 85)).size());
        assertEquals(5, collection.retrieve(greaterThanOrEqualTo(Car.CAR_ID, 95)).size());
        assertEquals(15, collection.retrieve(greaterThanOrEqualTo(Car.CAR_ID, 85)).size());
    }

    @Test
    public void indexQuantization_Between() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));

        Query<Car> query = between(Car.CAR_ID, 88, 92);
        assertEquals(5, collection.retrieve(query).size());
        assertEquals(asList(88, 89, 90, 91, 92), retrieveCarIds(collection, query));

        query = between(Car.CAR_ID, 88, true, 92, true);
        assertEquals(5, collection.retrieve(query).size());
        assertEquals(asList(88, 89, 90, 91, 92), retrieveCarIds(collection, query));

        query = between(Car.CAR_ID, 88, false, 92, true);
        assertEquals(4, collection.retrieve(query).size());
        assertEquals(asList(89, 90, 91, 92), retrieveCarIds(collection, query));

        query = between(Car.CAR_ID, 88, true, 92, false);
        assertEquals(4, collection.retrieve(query).size());
        assertEquals(asList(88, 89, 90, 91), retrieveCarIds(collection, query));

        query = between(Car.CAR_ID, 88, false, 92, false);
        assertEquals(3, collection.retrieve(query).size());
        assertEquals(asList(89, 90, 91), retrieveCarIds(collection, query));
    }

    @Test
    public void indexQuantization_ComplexQuery() {
        IndexedCollection<Car> collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10), Car.CAR_ID));
        collection.addAll(CarFactory.createCollectionOfCars(100));
        Query<Car> query = and(between(Car.CAR_ID, 96, 98), greaterThan(Car.CAR_ID, 95));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        assertEquals(10, collection.retrieve(query).getMergeCost());

        // 3 objects match the query...
        assertEquals(3, collection.retrieve(query).size());

        List<Integer> carIdsFound = retrieveCarIds(collection, query);
        assertEquals(asList(96, 97, 98), carIdsFound);
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
