/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.migrations;

import com.eventsourcing.Repository;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.inmem.MemoryJournal;
import com.eventsourcing.repository.LocalLockProvider;
import com.eventsourcing.repository.PackageCommandSetProvider;
import com.eventsourcing.repository.PackageEventSetProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class RepositoryTest {

    private final Package[] packages;
    protected Repository repository;
    protected LocalLockProvider lockProvider;
    protected NTPServerTimeProvider timeProvider;

    public RepositoryTest(Package ...packages) {
        this.packages = packages;
    }


    @BeforeMethod
    public void setUp() throws Exception {
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository = Repository.create();
        repository.setPhysicalTimeProvider(timeProvider);
        repository.setJournal(new MemoryJournal());
        repository.setIndexEngine(new MemoryIndexEngine());
        lockProvider = new LocalLockProvider();
        repository.setLockProvider(lockProvider);
        repository.startAsync().awaitRunning();
        // Add commands/events after the startup, to simulate production better
        repository.addCommandSetProvider(new PackageCommandSetProvider(packages));
        repository.addEventSetProvider(new PackageEventSetProvider(packages));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        repository.stopAsync().awaitTerminated();
    }
}