/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.googlecode.cqengine.index.support.CloseableIterator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;

public class JournalExporter {

    private final Repository repository;

    public JournalExporter(Repository repository) {this.repository = repository;}

    public void export(OutputStream stream, EntityWriter writer) throws IOException {
        Journal journal = repository.getJournal();
        Set<Class<? extends Command>> commands = repository.getCommands();
        for (Class<? extends Command> command : commands) {
            CloseableIterator<? extends EntityHandle<? extends Command>> iterator = journal
                    .commandIterator(command);
            while (iterator.hasNext()) {
                EntityHandle<? extends Command> commandHandle = iterator.next();
                writer.write(stream, Arrays.asList(commandHandle.get()));
                CloseableIterator<EntityHandle<Event>> eventsIterator = journal
                        .commandEventsIterator(commandHandle.uuid());
                writer.write(stream, (Iterable<Entity>) eventsIterator);
            }
        }
    }

}
