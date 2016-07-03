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
import com.eventsourcing.repository.RepositoryTest;
import com.eventsourcing.repository.StandardRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testng.annotations.Test;

@Test
public class PostgreSQLRepositoryTest extends RepositoryTest<Repository> {

    public PostgreSQLRepositoryTest() throws Exception {
        super(new StandardRepository());
    }

    @Override protected Journal createJournal() {
        return new PostgreSQLJournal(PostgreSQLTest.dataSource);
    }
}
