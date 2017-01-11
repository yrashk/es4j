/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.LocalLockProvider;
import com.eventsourcing.PackageCommandSetProvider;
import com.eventsourcing.PackageEventSetProvider;
import com.eventsourcing.Repository;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.inmem.MemoryIndexEngine;
import com.eventsourcing.inmem.MemoryJournal;
import com.eventsourcing.repository.StandardRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class RepositoryUsingTest {

    private final Package[] packages;
    protected Repository repository;
    protected LocalLockProvider lockProvider;
    protected NTPServerTimeProvider timeProvider;

    public RepositoryUsingTest(Package ...packages) {
        this.packages = packages;
    }


    @BeforeMethod
    public void setUp() throws Exception {
        repository =
        StandardRepository.builder()
                          .journal(new MemoryJournal())
                          .indexEngine(new MemoryIndexEngine()).build();
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
