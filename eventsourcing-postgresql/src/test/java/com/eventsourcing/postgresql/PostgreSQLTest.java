/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class PostgreSQLTest {
    public static final DataSource dataSource;

    static {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        String port = System.getenv("PGPORT") == null ? "5432" : System.getenv("PGPORT");
        ds.setUrl("jdbc:postgresql://localhost:" + port + "/eventsourcing?user=eventsourcing&password=eventsourcing");
        ds.setCurrentSchema("public");

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(100);
        config.setDataSource(ds);
        config.setLeakDetectionThreshold(2000);

        dataSource = new HikariDataSource(config);
    }
}
