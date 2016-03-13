package org.eventchain;

import lombok.SneakyThrows;
import org.eventchain.hlc.HybridTimestamp;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
        journal.journal((Command<?>) command.timestamp(timestamp), new Journal.Listener() {
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
