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
package org.eventchain.jmh;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import org.eventchain.models.Car;
import org.eventchain.models.CarFactory;
import org.h2.mvstore.MVStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.File;

@State(Scope.Benchmark)
public class HashIndexBenchmark {

    public static final String H2_FILENAME = "nio:HashIndexBenchmark.db";
    private org.eventchain.h2.index.HashIndex<String, Car> memoryH2Index;
    private org.eventchain.h2.index.HashIndex<String, Car> fileH2Index;
    private HashIndex<String, Car> memoryIndex;
    private IndexedCollection<Car> memoryCollection;
    private IndexedCollection<Car> memoryH2Collection;
    private IndexedCollection<Car> fileH2Collection;

    @Setup
    public void setup() {
        memoryIndex = HashIndex.onAttribute(Car.MANUFACTURER);
        memoryH2Index = org.eventchain.h2.index.HashIndex.onAttribute(MVStore.open(null), Car.MANUFACTURER);
        new File(H2_FILENAME).delete();
        fileH2Index = org.eventchain.h2.index.HashIndex.onAttribute(MVStore.open(H2_FILENAME), Car.MANUFACTURER);

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

    private void test(IndexedCollection<Car> coll) {
        Car car = CarFactory.createCar(1);
        coll.add(car);
    }
}
