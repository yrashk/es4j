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
package org.eventchain.examples.order;

import org.eventchain.*;
import org.eventchain.hlc.NTPServerTimeProvider;
import org.eventchain.index.MemoryIndexEngine;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class EventchainTest {

    private final Package pkg;
    protected Repository repository;
    protected MemoryLockProvider lockProvider;

    public EventchainTest() {
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