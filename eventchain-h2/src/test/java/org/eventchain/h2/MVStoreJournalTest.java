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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eventchain.Command;
import org.eventchain.Event;
import org.eventchain.Journal;
import org.eventchain.JournalTest;
import org.eventchain.hlc.HybridTimestamp;
import org.h2.mvstore.MVStore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@Slf4j
public class MVStoreJournalTest extends JournalTest<MVStoreJournal> {

    private final MVStore store;

    public MVStoreJournalTest() {
        super(new MVStoreJournal(MVStore.open(null)));
        store = journal.getStore();
    }

}