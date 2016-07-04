/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.Repository;
import com.eventsourcing.Journal;
import com.eventsourcing.index.CascadingIndexEngine;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.repository.RepositoryTest;
import com.eventsourcing.repository.StandardRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Test
public class PostgreSQLRepositoryTest extends RepositoryTest<Repository> {

    public PostgreSQLRepositoryTest() throws Exception {
        super(new StandardRepository());
    }

    @Override protected Journal createJournal() {
        return new PostgreSQLJournal(PostgreSQLTest.dataSource);
    }

    @SneakyThrows
    @Override protected IndexEngine createIndexEngine() {
        DataSource dataSource = PostgreSQLTest.dataSource;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement s = connection.prepareStatement("DROP SCHEMA IF EXISTS public CASCADE")) {
                s.executeUpdate();
            }
            try (PreparedStatement s = connection.prepareStatement("CREATE SCHEMA public")) {
                s.executeUpdate();
            }
        }
        return new CascadingIndexEngine(new PostgreSQLIndexEngine(dataSource), new MemoryIndexEngine());
    }
}
