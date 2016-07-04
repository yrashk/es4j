/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;


import com.googlecode.cqengine.index.support.CloseableIterator;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class PostgreSQLStatementIterator<T> implements CloseableIterator<T> {


    protected ResultSet resultSet;
    protected final PreparedStatement statement;
    protected final Connection connection;

    @SneakyThrows
    public PostgreSQLStatementIterator(PreparedStatement statement, Connection connection, boolean lazy) {
        this.statement = statement;
        this.connection = connection;
        if (!lazy) {
            resultSet = statement.executeQuery();
        }
    }

    @SneakyThrows
    @Override
    public boolean hasNext() {
        // lazy query execution
        if (resultSet == null) {
            resultSet = statement.executeQuery();
        }

        if (resultSet.next()) {
            return true;
        } else {
            close();
            return false;
        }
    }

    public abstract T next();

    @SneakyThrows
    @Override
    public void close() {
        if (resultSet != null && !resultSet.isClosed()) resultSet.close();
        if (!statement.isClosed()) statement.close();
        if (!connection.isClosed()) connection.close();
    }
}
