/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.Journal;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.inmem.MemoryIndexEngine;
import com.eventsourcing.postgresql.PostgreSQLJournal;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.zaxxer.hikari.HikariConfig;

public class PostgreSQLRepositoryBenchmark extends RepositoryBenchmark {
    @Override protected IndexEngine createIndex() {
        return new MemoryIndexEngine();
    }

    @Override protected Journal createJournal() {
        PGDataSource ds = new PGDataSource();
        ds.setHost("localhost");
        ds.setDatabase("eventsourcing");
        ds.setUser("eventsourcing");
        ds.setPassword("eventsourcing");
        ds.setPort(System.getenv("PGPORT") == null ? 5432 : Integer.valueOf(System.getenv("PGPORT")));
        ds.setHousekeeper(false);

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(30);

        return new PostgreSQLJournal(ds, config);
    }


}
