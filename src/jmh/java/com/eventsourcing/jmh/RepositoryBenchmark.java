/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.Repository;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.repository.Journal;
import com.eventsourcing.repository.MemoryLockProvider;
import com.eventsourcing.repository.PackageCommandSetProvider;
import com.eventsourcing.repository.PackageEventSetProvider;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;

@State(Scope.Benchmark)
public abstract class RepositoryBenchmark {

    private Repository repository;
    private Journal journal;
    private IndexEngine indexEngine;
    private MemoryLockProvider lockProvider;

    @Setup
    public void setup() throws Exception {
        repository = Repository.create();

        journal = createJournal();

        repository.setJournal(journal);

        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);

        indexEngine = createIndex();
        repository.setIndexEngine(indexEngine);

        lockProvider = new MemoryLockProvider();
        repository.setLockProvider(lockProvider);

        repository.addCommandSetProvider(
                new PackageCommandSetProvider(new Package[]{RepositoryBenchmark.class.getPackage()}));
        repository.addEventSetProvider(
                new PackageEventSetProvider(new Package[]{RepositoryBenchmark.class.getPackage()}));

        repository.startAsync().awaitRunning();
    }

    protected abstract IndexEngine createIndex();

    protected abstract Journal createJournal();

    @TearDown
    public void teardown() {
        repository.stopAsync().awaitTerminated();
    }


    @Benchmark
    @BenchmarkMode(Mode.All)
    public void basicPublish() throws ExecutionException, InterruptedException {
        repository.publish(new TestCommand()).get();
    }


}
