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
import com.eventsourcing.ResolvedEntityHandle;
import com.eventsourcing.StandardEntity;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.models.Car;
import com.eventsourcing.models.CarFactory;
import com.eventsourcing.queries.Max;
import com.eventsourcing.queries.Min;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.support.KeyStatistics;
import com.googlecode.cqengine.index.support.KeyStatisticsIndex;
import com.googlecode.cqengine.index.support.SortedKeyStatisticsIndex;
import com.googlecode.cqengine.quantizer.IntegerQuantizer;
import com.googlecode.cqengine.quantizer.Quantizer;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.SneakyThrows;
import org.apache.commons.net.ntp.TimeStamp;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.eventsourcing.queries.QueryFactory.max;
import static com.eventsourcing.queries.QueryFactory.min;
import static com.googlecode.cqengine.query.QueryFactory.*;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// This test was originally copied from CQEngine in order to test
// indices the same way CQEngine does.
public abstract class NavigableIndexTest<NavigableIndex extends AttributeIndex & SortedKeyStatisticsIndex> {

    public abstract <A extends Comparable<A>, O extends Entity> NavigableIndex onAttribute(Attribute<O, A> attribute);

    public abstract <A extends Comparable<A>, O extends Entity> Index<EntityHandle<O>>
           withQuantizerOnAttribute(Quantizer<A> quantizer, Attribute<O, A> attribute);

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
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, EntityHandle<Car>> MODEL_INDEX = onAttribute(Car.MODEL);
        MODEL_INDEX.clear(noQueryOptions());
        collection.addIndex(MODEL_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<String> distinctModels = setOf(MODEL_INDEX.getDistinctKeys(noQueryOptions()));
        assertEquals(new ArrayList<>(distinctModels),
                     asList("Accord", "Avensis", "Civic", "Focus", "Fusion", "Hilux", "Insight", "M6", "Prius",
                            "Taurus"));
        for (String model : distinctModels) {
            assertEquals(MODEL_INDEX.getCountForKey(model, noQueryOptions()), Integer.valueOf(2));
        }

        Set<String> distinctModelsDescending = setOf(MODEL_INDEX.getDistinctKeysDescending(noQueryOptions()));
        assertEquals(new ArrayList<>(distinctModelsDescending),
                     asList("Taurus", "Prius", "M6", "Insight", "Hilux", "Fusion", "Focus", "Civic", "Avensis",
                            "Accord"));
    }

    @Test
    public void getCountOfDistinctKeys() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, EntityHandle<Car>> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        assertEquals(MANUFACTURER_INDEX.getCountOfDistinctKeys(noQueryOptions()), Integer.valueOf(4));
    }

    @Test
    public void getStatisticsForDistinctKeys() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<String, EntityHandle<Car>> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
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

        ));
    }

    @Test
    public void getStatisticsForDistinctKeysDescending() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, EntityHandle<Car>> MANUFACTURER_INDEX = onAttribute(Car.MANUFACTURER);
        MANUFACTURER_INDEX.clear(noQueryOptions());
        collection.addIndex(MANUFACTURER_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(20));

        Set<KeyStatistics<String>> keyStatistics = setOf(
                MANUFACTURER_INDEX.getStatisticsForDistinctKeysDescending(noQueryOptions()));
        assertEquals(keyStatistics, setOf(
                new KeyStatistics<>("Toyota", 6),
                new KeyStatistics<>("Honda", 6),
                new KeyStatistics<>("Ford", 6),
                new KeyStatistics<>("BMW", 2)

        ));
    }

    @Test
    public void retrieveLess() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, EntityHandle<Car>> PRICE_INDEX = onAttribute(Car.PRICE);
        PRICE_INDEX.clear(noQueryOptions());
        collection.addIndex(PRICE_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(10));

        try (ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(lessThan(Car.PRICE, 3999.99))) {
            assertEquals(resultSet.size(), 1);
            assertEquals(resultSet.uniqueResult().get().getModel(), "Accord");
        }

        try (ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(lessThanOrEqualTo(Car.PRICE, 3999.99))) {
            assertEquals(resultSet.size(), 2);
            ArrayList<EntityHandle<Car>> values = Lists.newArrayList(resultSet.iterator());
            assertTrue(values.stream().anyMatch(h -> h.get().getModel().contentEquals("Fusion")));
            assertTrue(values.stream().anyMatch(h -> h.get().getModel().contentEquals("Accord")));
        }

    }

    @Test
    public void retrieveGreater() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, EntityHandle<Car>> PRICE_INDEX = onAttribute(Car.PRICE);
        PRICE_INDEX.clear(noQueryOptions());
        collection.addIndex(PRICE_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(10));

        try (ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(greaterThan(Car.PRICE, 8500.00))) {
            assertEquals(resultSet.size(), 1);
            assertEquals(resultSet.uniqueResult().get().getModel(), "M6");
        }

        try (ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(greaterThanOrEqualTo(Car.PRICE, 8500.00))) {
            assertEquals(resultSet.size(), 2);
            ArrayList<EntityHandle<Car>> values = Lists.newArrayList(resultSet.iterator());
            assertTrue(values.stream().anyMatch(h -> h.get().getModel().contentEquals("M6")));
            assertTrue(values.stream().anyMatch(h -> h.get().getModel().contentEquals("Prius")));
        }

    }


    @Test
    public void retrieveBetween() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<String, EntityHandle<Car>> PRICE_INDEX = onAttribute(Car.PRICE);
        PRICE_INDEX.clear(noQueryOptions());
        collection.addIndex(PRICE_INDEX);

        collection.addAll(CarFactory.createCollectionOfCars(10));

        try (ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(between(Car.PRICE, 8000.00, 9001.00))) {
            assertEquals(resultSet.size(), 2);
            ArrayList<EntityHandle<Car>> values = Lists.newArrayList(resultSet.iterator());
            assertTrue(values.stream().anyMatch(h -> h.get().getModel().contentEquals("M6")));
            assertTrue(values.stream().anyMatch(h -> h.get().getModel().contentEquals("Prius")));
        }
    }

    @Test
    public void indexQuantization_SpanningTwoBucketsMidRange() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 20 because this query spans 2 buckets (each containing 10 objects)...
        ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(between(Car.CAR_ID, 47, 53));
        assertEquals(resultSet.getMergeCost(), 20);
        resultSet.close();

        // 7 objects match the query (between is inclusive)...
        resultSet = collection.retrieve(between(Car.CAR_ID, 47, 53));
        assertEquals(resultSet.size(), 7);
        resultSet.close();

        // The matching objects are...
        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 47, 53));
        assertEquals(carIdsFound, asList(47, 48, 49, 50, 51, 52, 53));
    }

    @Test
    public void indexQuantization_FirstBucket() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(between(Car.CAR_ID, 2, 4));
        assertEquals(resultSet.getMergeCost(), 10);
        resultSet.close();

        // 3 objects match the query...
        resultSet = collection.retrieve(between(Car.CAR_ID, 2, 4));
        assertEquals(resultSet.size(), 3);
        resultSet.close();

        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 2, 4));
        assertEquals(carIdsFound, asList(2, 3, 4));
    }

    @Test
    public void indexQuantization_LastBucket() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(between(Car.CAR_ID, 96, 98));
        assertEquals(resultSet.getMergeCost(), 10);
        resultSet.close();

        // 3 objects match the query...
        resultSet = collection.retrieve(between(Car.CAR_ID, 96, 98));
        assertEquals(resultSet.size(), 3);
        resultSet.close();

        List<Integer> carIdsFound = retrieveCarIds(collection, between(Car.CAR_ID, 96, 98));
        assertEquals(carIdsFound, asList(96, 97, 98));
    }


    @Test
    public void indexQuantization_LessThan() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));

        assertEquals(collection.retrieve(lessThan(Car.CAR_ID, 5)).size(), 5);
        assertEquals(collection.retrieve(lessThan(Car.CAR_ID, 15)).size(), 15);
        assertEquals(collection.retrieve(lessThanOrEqualTo(Car.CAR_ID, 5)).size(), 6);
        assertEquals(collection.retrieve(lessThanOrEqualTo(Car.CAR_ID, 15)).size(), 16);
    }

    @Test
    public void indexQuantization_GreaterThan() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));

        ResultSet<EntityHandle<Car>> resultSet;

        resultSet = collection.retrieve(greaterThan(Car.CAR_ID, 95));
        assertEquals(resultSet.size(), 4);
        resultSet.close();

        resultSet = collection.retrieve(greaterThan(Car.CAR_ID, 85));
        assertEquals(resultSet.size(), 14);
        resultSet.close();

        resultSet = collection.retrieve(greaterThanOrEqualTo(Car.CAR_ID, 95));
        assertEquals(resultSet.size(), 5);
        resultSet.close();

        resultSet = collection.retrieve(greaterThanOrEqualTo(Car.CAR_ID, 85));
        assertEquals(resultSet.size(), 15);
        resultSet.close();
    }

    @Test
    public void indexQuantization_Between() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));

        Query<EntityHandle<Car>> query = between(Car.CAR_ID, 88, 92);
        assertEquals(collection.retrieve(query).size(), 5);
        assertEquals(retrieveCarIds(collection, query), asList(88, 89, 90, 91, 92));

        query = between(Car.CAR_ID, 88, true, 92, true);
        ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(query);
        assertEquals(resultSet.size(), 5);
        resultSet.close();

        assertEquals(retrieveCarIds(collection, query), asList(88, 89, 90, 91, 92));

        query = between(Car.CAR_ID, 88, false, 92, true);
        resultSet = collection.retrieve(query);
        assertEquals(resultSet.size(), 4);
        resultSet.close();

        assertEquals(retrieveCarIds(collection, query), asList(89, 90, 91, 92));

        query = between(Car.CAR_ID, 88, true, 92, false);
        resultSet = collection.retrieve(query);
        assertEquals(resultSet.size(), 4);
        resultSet.close();
        assertEquals(retrieveCarIds(collection, query), asList(88, 89, 90, 91));

        query = between(Car.CAR_ID, 88, false, 92, false);
        resultSet = collection.retrieve(query);
        assertEquals(resultSet.size(), 3);
        resultSet.close();
        assertEquals(retrieveCarIds(collection, query), asList(89, 90, 91));
    }

    @Test
    public void indexQuantization_ComplexQuery() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        Index<EntityHandle<Car>> index = withQuantizerOnAttribute(IntegerQuantizer.withCompressionFactor(10),
                                                                  Car.CAR_ID);
        index.clear(noQueryOptions());
        collection.addIndex(index);
        collection.addAll(CarFactory.createCollectionOfCars(100));
        Query<EntityHandle<Car>> query = and(between(Car.CAR_ID, 96, 98), greaterThan(Car.CAR_ID, 95));

        // 3 objects match the query...
        ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(query);
        assertEquals(resultSet.size(), 3);
        resultSet.close();

        List<Integer> carIdsFound = retrieveCarIds(collection, query);
        assertEquals(carIdsFound, asList(96, 97, 98));

        // Merge cost should be 10, because objects matching this query are in a single bucket...
        resultSet = collection.retrieve(query);
        assertEquals(resultSet.getMergeCost(), 10);
        resultSet.close();
    }

    static List<Integer> retrieveCarIds(IndexedCollection<EntityHandle<Car>> collection, Query<EntityHandle<Car>> query) {
        ResultSet<EntityHandle<Car>> cars = collection.retrieve(query, queryOptions(orderBy(ascending(Car.CAR_ID))));
        List<Integer> carIds = new ArrayList<>();
        for (EntityHandle<Car> car : cars) {
            carIds.add(car.get().getCarId());
        }
        cars.close();
        return carIds;
    }

    @Test
    @SneakyThrows
    public void serializableComparable() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        NavigableIndex index = onAttribute(Car.TIMESTAMP);
        index.clear(noQueryOptions());
        collection.addIndex(index);

        Car car1 = CarFactory.createCar(1);
        Car car2 = CarFactory.createCar(2);

        NTPServerTimeProvider ntpServerTimeProvider = new NTPServerTimeProvider();
        ntpServerTimeProvider.startAsync().awaitRunning();

        HybridTimestamp ts1 = new HybridTimestamp(ntpServerTimeProvider);
        HybridTimestamp ts2 = ts1.clone();
        Thread.sleep(1000);
        ts2.update();

        assertTrue(ts2.compareTo(ts1) > 0);
        assertTrue(ts2.getSerializableComparable().compareTo(ts1.getSerializableComparable()) > 0);

        car1.timestamp(ts1);
        car2.timestamp(ts2);

        collection.add(new ResolvedEntityHandle<>(car1));
        collection.add(new ResolvedEntityHandle<>(car2));

        try (ResultSet<EntityHandle<Car>> resultSet = collection.retrieve(greaterThan(Car.TIMESTAMP, ts1))) {
            assertEquals(resultSet.size(), 1);
            assertEquals(resultSet.uniqueResult().get().getModel(), "Taurus");
        }

        index.clear(noQueryOptions());

        ntpServerTimeProvider.stopAsync().awaitTerminated();

    }

    @Test
    public void reindexData() {
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<Double, EntityHandle<Car>> PRICE_INDEX =
                (KeyStatisticsIndex<Double, EntityHandle<Car>>) onAttribute(Car.PRICE);
        PRICE_INDEX.clear(noQueryOptions());

        Set<EntityHandle<Car>> cars = CarFactory.createCollectionOfCars(10);

        collection.addAll(cars);

        collection.addIndex(PRICE_INDEX);

        IndexedCollection<EntityHandle<Car>> collection1 = new ConcurrentIndexedCollection<>();
        KeyStatisticsIndex<Double, EntityHandle<Car>> PRICE_INDEX_1 =
                (KeyStatisticsIndex<Double, EntityHandle<Car>>) onAttribute(Car.PRICE);

        collection1.addAll(cars);

        collection1.addIndex(PRICE_INDEX_1);


        assertEquals((int)PRICE_INDEX.getCountForKey(9000.23, noQueryOptions()), 1);
        assertEquals((int)PRICE_INDEX_1.getCountForKey(9000.23, noQueryOptions()), 1);

        PRICE_INDEX.clear(noQueryOptions());
        PRICE_INDEX_1.clear(noQueryOptions());
    }

    @Test
    public void minMax() {
        Random random = new Random();
        IndexedCollection<EntityHandle<Car>> collection = new ConcurrentIndexedCollection<>();
        SortedKeyStatisticsIndex<HybridTimestamp, EntityHandle<Car>> MODEL_INDEX = onAttribute(Car.TIMESTAMP);
        WrappedSimpleIndex<Car, HybridTimestamp> i = new WrappedSimpleIndex<>((Function<Car, HybridTimestamp>) StandardEntity::timestamp);
        i.setAttribute(Car.TIMESTAMP);
        if (MODEL_INDEX.supportsQuery(new Min<>(i), noQueryOptions()) &&
            MODEL_INDEX.supportsQuery(new Max<>(i), noQueryOptions())) {
            MODEL_INDEX.clear(noQueryOptions());
            collection.addIndex(MODEL_INDEX);

            QueryOptions queryOptions = queryOptions();
            queryOptions.put(IndexedCollection.class, collection);

            assertTrue(collection.retrieve(max(i), queryOptions).isEmpty());
            assertTrue(collection.retrieve(min(i), queryOptions).isEmpty());

            Set<EntityHandle<Car>> cars1 = CarFactory.createCollectionOfCars(100_000);
            cars1.forEach(car -> car.get().timestamp(new HybridTimestamp(random.nextInt(), 0)));
            collection.addAll(cars1);

            long t1 = System.nanoTime();
            HybridTimestamp max1 = collection.retrieve(max(i), queryOptions).uniqueResult().get().timestamp();
            HybridTimestamp min1 = collection.retrieve(min(i), queryOptions).uniqueResult().get().timestamp();
            long t2 = System.nanoTime();

            assertFalse(cars1.stream().anyMatch(c -> c.get().timestamp().getSerializableComparable().compareTo(max1.getSerializableComparable()) > 0));
            assertFalse(cars1.stream().anyMatch(c -> c.get().timestamp().getSerializableComparable().compareTo(min1.getSerializableComparable()) < 0));

            Set<EntityHandle<Car>> cars2 = CarFactory.createCollectionOfCars(100_000);
            cars2.forEach(car -> car.get().timestamp(new HybridTimestamp(random.nextInt(), random.nextInt(2))));
            collection.addAll(cars2);

            long t1_ = System.nanoTime();
            HybridTimestamp max2 = collection.retrieve(max(i), queryOptions).uniqueResult().get().timestamp();
            HybridTimestamp min2 = collection.retrieve(min(i), queryOptions).uniqueResult().get().timestamp();
            long t2_ = System.nanoTime();

            assertFalse(cars2.stream().anyMatch(c -> c.get().timestamp().getSerializableComparable().compareTo(max2.getSerializableComparable()) > 0));
            assertFalse(cars2.stream().anyMatch(c -> c.get().timestamp().getSerializableComparable().compareTo(min2.getSerializableComparable()) < 0));

            MODEL_INDEX.clear(noQueryOptions());

        }
    }

}
