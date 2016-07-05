/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.Lock;
import com.eventsourcing.LockProvider;
import com.google.common.util.concurrent.AbstractService;
import lombok.Getter;
import lombok.SneakyThrows;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of {@link LockProvider} that uses PostgreSQL's
 * <a href="https://www.postgresql.org/docs/9.5/static/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS">explicit locking</a>
 * functionality
 */
@Component(property = {"type=PostgreSQLLockProvider"})
public class PostgreSQLLockProvider extends AbstractService implements LockProvider {

    @Reference
    protected DataSourceProvider dataSourceProvider;

    private DataSource dataSource;

    @Activate
    protected void activate() {
        dataSource = dataSourceProvider.getDataSource();
    }

    public PostgreSQLLockProvider() {}

    @Override protected void doStart() {
        notifyStarted();
    }

    @Override protected void doStop() {
        notifyStopped();
    }

    public PostgreSQLLockProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override public Lock lock(Object lock) {
        return new PostgreSQLLock(dataSource.getConnection(), lock.hashCode());
    }

    public class PostgreSQLLock implements Lock {

        private final Connection connection;
        private final long key;

        @Getter
        private boolean locked;

        public PostgreSQLLock(Connection connection, long key) throws SQLException {
            this.connection = connection;
            this.key = key;
            this.locked = true;
            try (PreparedStatement s = connection.prepareStatement("SELECT pg_advisory_lock(?)")) {
                s.setLong(1, key);
                s.execute();
            }
        }

        @SneakyThrows
        @Override public void unlock() {
            try (PreparedStatement s = connection.prepareStatement("SELECT pg_advisory_unlock(?)")) {
                s.setLong(1, key);
                s.execute();
            }
            locked = false;
        }

    }
}
