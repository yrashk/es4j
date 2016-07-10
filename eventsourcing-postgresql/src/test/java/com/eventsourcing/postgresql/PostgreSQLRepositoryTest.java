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
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.repository.RepositoryTest;
import com.eventsourcing.repository.StandardRepository;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.eventsourcing.postgresql.PostgreSQLTest.dataSource;

@Test
public class PostgreSQLRepositoryTest extends RepositoryTest<Repository> {

    public PostgreSQLRepositoryTest() throws Exception {
        super(new StandardRepository());
    }

    @SneakyThrows
    @Override protected Journal createJournal() {
        recreateSchema();
        return new PostgreSQLJournal(dataSource);
    }

    @SneakyThrows
    @Override protected IndexEngine createIndexEngine() {
        recreateSchema();
        return new CascadingIndexEngine(new PostgreSQLIndexEngine(dataSource), new MemoryIndexEngine());
    }

    private void recreateSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement s = connection.prepareStatement("DROP SCHEMA IF EXISTS public CASCADE")) {
                s.executeUpdate();
            }
            try (PreparedStatement s = connection.prepareStatement("CREATE SCHEMA public")) {
                s.executeUpdate();
            }
        }
    }

}
