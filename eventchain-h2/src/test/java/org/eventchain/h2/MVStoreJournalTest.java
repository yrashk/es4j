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
package org.eventchain.h2;

import lombok.Getter;
import lombok.Setter;
import org.eventchain.Event;
import org.eventchain.JournalTest;
import org.h2.mvstore.MVStore;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class MVStoreJournalTest extends JournalTest<MVStoreJournal> {

    private final MVStore store;

    public MVStoreJournalTest() {
        super(new MVStoreJournal(MVStore.open(null)));
        store = journal.getStore();
    }

    public class TestEvent extends Event {
        @Getter @Setter
        private String value;
    }

    @Test
    public void unrecognizedEntities() {
        journal.clear();
        journal.onEventsAdded(Collections.singletonList(TestEvent.class).stream().collect(Collectors.toSet()));
        journal = new MVStoreJournal(store);
        journal.initializeStore();
        List<MVStoreJournal.LayoutInformation> unrecognizedEntities = journal.getUnrecognizedEntities();
        assertFalse(unrecognizedEntities.isEmpty());
        assertEquals(unrecognizedEntities.size(), 1);
        MVStoreJournal.LayoutInformation entity = unrecognizedEntities.get(0);
        assertEquals(entity.className(), TestEvent.class.getName());
        assertEquals(entity.properties().get(1).name(), "value");
        assertEquals(entity.properties().get(1).type(), "java.lang.String");
    }
}