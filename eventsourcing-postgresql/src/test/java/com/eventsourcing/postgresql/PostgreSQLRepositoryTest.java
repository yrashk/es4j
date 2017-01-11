/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.Journal;
import com.eventsourcing.Repository;
import com.eventsourcing.index.CascadingIndexEngine;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.inmem.MemoryIndexEngine;
import com.eventsourcing.repository.RepositoryTest;
import com.eventsourcing.repository.StandardRepository;
import com.impossibl.postgres.jdbc.PGDataSource;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import static com.eventsourcing.postgresql.PostgreSQLTest.createDataSource;

@Test
public class PostgreSQLRepositoryTest extends RepositoryTest<Repository> {

    private PGDataSource dataSource;

    public PostgreSQLRepositoryTest() throws Exception {
        super(new StandardRepository());
    }

    @SneakyThrows
    @Override protected Journal createJournal() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return new PostgreSQLJournal(dataSource);
    }

    @SneakyThrows
    @Override protected IndexEngine createIndexEngine() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return new CascadingIndexEngine(new PostgreSQLIndexEngine(dataSource), new MemoryIndexEngine());
    }


}
