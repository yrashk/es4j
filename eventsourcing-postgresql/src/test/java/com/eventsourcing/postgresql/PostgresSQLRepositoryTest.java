/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.Repository;
import com.eventsourcing.RepositoryTest;
import com.eventsourcing.repository.Journal;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testng.annotations.Test;

@Test
public class PostgresSQLRepositoryTest extends RepositoryTest<Repository> {

    public PostgresSQLRepositoryTest() throws Exception {
        super(Repository.create());
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
