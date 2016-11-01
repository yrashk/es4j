/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.*;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.inmem.MemoryJournal;
import com.eventsourcing.repository.StandardRepository;
import com.googlecode.cqengine.IndexedCollection;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.eventsourcing.index.EntityQueryFactory.*;
import static org.testng.Assert.assertTrue;

public abstract class IndexEngineTest<T extends IndexEngine> {

    private final T indexEngine;
    private final Repository repository;
    private final Journal journal;
    private final NTPServerTimeProvider timeProvider;

    @SneakyThrows
    public IndexEngineTest(T indexEngine) {
        this.indexEngine = indexEngine;

        repository = new StandardRepository();
        journal = new MemoryJournal();
        journal.setRepository(repository);
        repository.setJournal(journal);
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{getClass().getPackage()}));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{getClass().getPackage()}));
        repository.setIndexEngine(indexEngine);
        repository.setLockProvider(new LocalLockProvider());
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository.setPhysicalTimeProvider(timeProvider);

        indexEngine.setJournal(journal);
        indexEngine.setRepository(repository);
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        repository.startAsync().awaitRunning();
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        repository.stopAsync().awaitTerminated();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        journal.clear();
    }


    @EqualsAndHashCode(callSuper = false)
    public static class TestEvent extends StandardEvent {
        @Getter
        private final String string;

        @Builder
        public TestEvent(String string, HybridTimestamp timestamp, String anotherString) {
            super(timestamp);
            this.string = string;
            this.anotherString = anotherString;
        }

        @Getter @Accessors(chain = true)
        private final String anotherString;

        public final static SimpleIndex<TestEvent, String> ANOTHER_ATTR = SimpleIndex.as(TestEvent::getAnotherString);

        @Index
        public final static SimpleIndex<TestEvent, String> ATTR = SimpleIndex.as(TestEvent::getString);
    }

    @Accessors(fluent = true)
    public static class TestCommand extends StandardCommand<Void, Void> {
        @Getter
        private final String string;

        @Builder
        public TestCommand(HybridTimestamp timestamp, String string) {
            super(timestamp);
            this.string = string;
        }

        @Override
        public EventStream<Void> events() {
            return EventStream.of(TestEvent.builder().string(string).build());
        }
    }


}