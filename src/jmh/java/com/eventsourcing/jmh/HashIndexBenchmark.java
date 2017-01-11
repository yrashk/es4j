/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.ResolvedEntityHandle;
import com.eventsourcing.jmh.models.Car;
import com.eventsourcing.jmh.models.CarFactory;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.File;

@State(Scope.Benchmark)
public class HashIndexBenchmark {

    public static final String H2_FILENAME = "nio:HashIndexBenchmark.db";
    private HashIndex<String, EntityHandle<Car>> memoryIndex;
    private IndexedCollection<EntityHandle<Car>> memoryCollection;

    @Setup
    public void setup() {
        memoryIndex = HashIndex.onAttribute(Car.MANUFACTURER);

        memoryCollection = new ConcurrentIndexedCollection<>();
        memoryCollection.addIndex(memoryIndex);

    }

    @Benchmark
    public void t01memory() {
        test(memoryCollection);
    }

    @Benchmark
    private void test(IndexedCollection<EntityHandle<Car>> coll) {
        Car car = CarFactory.createCar(1);
        coll.add(new ResolvedEntityHandle<>(car));
    }
}
