/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.jmh.models.Car;
import com.eventsourcing.jmh.models.CarFactory;
import com.eventsourcing.ResolvedEntityHandle;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import org.h2.mvstore.MVStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.File;

@State(Scope.Benchmark)
public class HashIndexBenchmark {

    public static final String H2_FILENAME = "nio:HashIndexBenchmark.db";
    private com.eventsourcing.h2.index.HashIndex<String, Car> memoryH2Index;
    private com.eventsourcing.h2.index.HashIndex<String, Car> fileH2Index;
    private HashIndex<String, EntityHandle<Car>> memoryIndex;
    private IndexedCollection<EntityHandle<Car>> memoryCollection;
    private IndexedCollection<EntityHandle<Car>> memoryH2Collection;
    private IndexedCollection<EntityHandle<Car>> fileH2Collection;

    @Setup
    public void setup() {
        memoryIndex = HashIndex.onAttribute(Car.MANUFACTURER);
        memoryH2Index = com.eventsourcing.h2.index.HashIndex.onAttribute(MVStore.open(null), Car.MANUFACTURER);
        new File(H2_FILENAME).delete();
        fileH2Index = com.eventsourcing.h2.index.HashIndex.onAttribute(MVStore.open(H2_FILENAME), Car.MANUFACTURER);

        memoryCollection = new ConcurrentIndexedCollection<>();
        memoryCollection.addIndex(memoryIndex);

        memoryH2Collection = new ConcurrentIndexedCollection<>();
        memoryH2Collection.addIndex(memoryH2Index);

        fileH2Collection = new ConcurrentIndexedCollection<>();
        fileH2Collection.addIndex(fileH2Index);
    }

    @Benchmark
    public void t01memory() {
        test(memoryCollection);
    }

    @Benchmark
    public void t02memoryH2() {
        test(memoryH2Collection);
    }

    @Benchmark
    public void t03fileH2() {
        test(fileH2Collection);
    }

    private void test(IndexedCollection<EntityHandle<Car>> coll) {
        Car car = CarFactory.createCar(1);
        coll.add(new ResolvedEntityHandle<>(car));
    }
}
