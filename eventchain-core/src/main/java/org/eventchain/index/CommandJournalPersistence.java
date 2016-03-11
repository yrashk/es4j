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

import org.eventchain.*;

import java.util.Iterator;
import java.util.Set;

public class CommandJournalPersistence<T extends Command<?>> extends JournalPersistence<T> {
    public CommandJournalPersistence(Journal journal, Class<T> klass) {
        super(journal, klass);
    }

    @Override
    public Set<EntityHandle<T>> create() {
        return new CommandJournalSet<>(journal, klass);
    }

    static class CommandJournalSet<T extends Command<?>> extends JournalSet<T> {

        public CommandJournalSet(Journal journal, Class<T> klass) {
            super(journal, klass);
        }

        @Override
        public Iterator<EntityHandle<T>> iterator() {
            return journal.commandIterator(klass);
        }
    }
}
