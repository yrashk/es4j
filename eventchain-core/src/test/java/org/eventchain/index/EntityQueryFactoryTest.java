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

import lombok.SneakyThrows;
import org.eventchain.*;
import org.eventchain.hlc.NTPServerTimeProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EntityQueryFactoryTest {

    protected Repository repository;

    @BeforeMethod
    public void setUp() throws Exception {
        repository = Repository.create();
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{EntityQueryFactoryTest.class.getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{EntityQueryFactoryTest.class.getPackage()}));
        repository.setJournal(new MemoryJournal());
        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider();
        repository.setPhysicalTimeProvider(timeProvider);
        repository.setIndexEngine(new MemoryIndexEngine());
        repository.setLockProvider(new MemoryLockProvider());
        repository.startAsync().awaitRunning();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        repository.stopAsync().awaitTerminated();
    }

    public static class TestCommand extends Command<Void> {}

    @Test @SneakyThrows
    public void all() {
        repository.publish(new TestCommand()).get();
        ResultSet<EntityHandle<TestCommand>> resultSet = repository.query(TestCommand.class, EntityQueryFactory.all(TestCommand.class));
        assertTrue(resultSet.isNotEmpty());
        assertEquals(resultSet.size(), 1);
    }


}