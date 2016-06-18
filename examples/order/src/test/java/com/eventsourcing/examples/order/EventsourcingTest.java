/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order;

import com.eventsourcing.*;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.repository.MemoryJournal;
import com.eventsourcing.repository.MemoryLockProvider;
import com.eventsourcing.repository.PackageCommandSetProvider;
import com.eventsourcing.repository.PackageEventSetProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class EventsourcingTest {

    private final Package pkg;
    protected Repository repository;
    protected MemoryLockProvider lockProvider;

    public EventsourcingTest() {
        this.pkg = Order.class.getPackage();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        repository = Repository.create();
        repository.setJournal(new MemoryJournal());
        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);
        repository.setIndexEngine(new MemoryIndexEngine());
        lockProvider = new MemoryLockProvider();
        repository.setLockProvider(lockProvider);
        repository.startAsync().awaitRunning();
        // Add commands/events after the startup, to simulate production better
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{pkg}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{pkg}));

    }

    @AfterMethod
    public void tearDown() throws Exception {
        repository.stopAsync().awaitTerminated();
    }
}