/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh;

import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.postgresql.PostgreSQLJournal;
import com.eventsourcing.Journal;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

public class PostgreSQLRepositoryBenchmark extends RepositoryBenchmark {
    @Override protected IndexEngine createIndex() {
        return new MemoryIndexEngine();
    }

    @Override protected Journal createJournal() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost/eventsourcing?user=eventsourcing&password=eventsourcing");

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(30);
        config.setDataSource(dataSource);

        return new PostgreSQLJournal(new HikariDataSource(config));
    }


}
