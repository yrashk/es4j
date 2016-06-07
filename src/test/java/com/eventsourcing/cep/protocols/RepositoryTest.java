package com.eventsourcing.cep.protocols;

import com.eventsourcing.*;
import com.eventsourcing.index.MemoryIndexEngine;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class RepositoryTest {

    private final Package[] packages;
    protected Repository repository;
    protected MemoryLockProvider lockProvider;

    public RepositoryTest(Package ...packages) {
        this.packages = packages;
    }


    @BeforeMethod
    public void setUp() throws Exception {
        repository = Repository.create();
        repository.setJournal(new MemoryJournal());
        repository.setIndexEngine(new MemoryIndexEngine());
        lockProvider = new MemoryLockProvider();
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
