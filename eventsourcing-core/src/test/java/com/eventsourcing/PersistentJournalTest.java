/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.repository.Journal;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

public abstract class PersistentJournalTest<T extends Journal> extends JournalTest<T> {
    public PersistentJournalTest(T journal) {
        super(journal);
    }

    /**
     * Close and re-open the journal.
     */
    public abstract void reopen();

    /**
     * Close and re-opens another journal.
     */
    public abstract void reopenAnother();

    @Test
    public void persistence() {
        persistenceTest(this::reopen, true);
    }

    @Test
    public void persistenceWrongJournal() {
        persistenceTest(this::reopenAnother, false);
        reopen();
    }

    @SneakyThrows
    private void persistenceTest(Runnable r, boolean works) {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
        List<Event> events = new ArrayList<>();
        TestCommand command = new TestCommand(true);
        journal.journal((StandardCommand<?>) command.timestamp(timestamp), new Journal.Listener() {
            @Override
            public void onEvent(Event event) {
                events.add(event);
            }
        });
        assertEquals(events.size(), 1);

        r.run();

        Optional<Entity> entity = journal.get(command.uuid());
        if (works) {
            assertTrue(entity.isPresent());
            assertEquals(command.uuid(), entity.get().uuid());
        } else {
            assertFalse(entity.isPresent());
        }

        Event event = events.get(0);
        Optional<Entity> eventEntity = journal.get(event.uuid());
        if (works) {
            assertTrue(eventEntity.isPresent());
            assertEquals(event.uuid(), eventEntity.get().uuid());
        } else {
            assertFalse(eventEntity.isPresent());
        }
    }

}
