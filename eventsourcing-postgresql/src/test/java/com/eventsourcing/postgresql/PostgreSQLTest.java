/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.impossibl.postgres.jdbc.PGDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class PostgreSQLTest {
    public static final DataSource dataSource;

    static {
        PGDataSource ds = new PGDataSource();
        ds.setHost("localhost");
        ds.setDatabase("eventsourcing");
        ds.setUser("eventsourcing");
        ds.setPassword("eventsourcing");
        ds.setPort(System.getenv("PGPORT") == null ? 5432 : Integer.valueOf(System.getenv("PGPORT")));
        ds.setHousekeeper(false);

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(50);
        config.setDataSource(ds);
        config.setLeakDetectionThreshold(3000);
        config.setConnectionInitSql("SET log_statement = 'all'");

        dataSource = new HikariDataSource(config);
    }
}
