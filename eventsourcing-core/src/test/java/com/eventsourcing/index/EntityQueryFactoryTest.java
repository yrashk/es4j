/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.*;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.SneakyThrows;
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
        NTPServerTimeProvider timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
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
        ResultSet<EntityHandle<TestCommand>> resultSet = repository.query(TestCommand.class, EntityQueryFactory.all(TestCommand.class));
        assertTrue(resultSet.isEmpty());
        repository.publish(new TestCommand()).get();
        resultSet = repository.query(TestCommand.class, EntityQueryFactory.all(TestCommand.class));
        assertTrue(resultSet.isNotEmpty());
        assertEquals(resultSet.size(), 1);
    }


}