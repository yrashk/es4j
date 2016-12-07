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
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.CQIndexEngine;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.postgresql.index.EqualityIndex;
import com.eventsourcing.postgresql.index.NavigableIndex;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.zaxxer.hikari.HikariConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Component(property = {"type=PostgreSQLIndexEngine"})
public class PostgreSQLIndexEngine extends CQIndexEngine implements IndexEngine {

    @Override public String getType() {
        return "PostgreSQLIndexEngine";
    }

    @Reference
    protected DataSourceProvider dataSourceProvider;

    private HikariConfig hikariConfig;
    private DataSource dataSource;

    public PostgreSQLIndexEngine() {}


    public PostgreSQLIndexEngine(PGDataSource dataSource) {
        this.dataSourceProvider = () -> dataSource;
    }
    public PostgreSQLIndexEngine(PGDataSource dataSource, HikariConfig hikariConfig) {
        this.dataSourceProvider = () -> dataSource;
        this.hikariConfig = hikariConfig;
    }


    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
        PooledDataSource pooledDataSource = PooledDataSource.getInstance(repository);
        if (hikariConfig != null) {
            pooledDataSource.setHikariConfig(hikariConfig);
        }
        pooledDataSource.getHikariConfig().setDataSource(dataSourceProvider.getDataSource());
        dataSource = pooledDataSource.getDataSource();
    }

    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.journal = journal;
    }


    @Override
    protected void doStop() {
        super.doStop();
    }

    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Arrays.asList(
                new IndexCapabilities<Attribute>("Hash",
                                                 new IndexFeature[]{EQ, IN, QZ},
                                                 attribute -> EqualityIndex.onAttribute(dataSource, attribute, false)),
                new IndexCapabilities<Attribute>("Unique",
                                                 new IndexFeature[]{EQ, IN, QZ, UNIQUE},
                                                 attribute -> EqualityIndex.onAttribute(dataSource, attribute, true)),
                new IndexCapabilities<Attribute>("Navigable",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.QZ, IndexFeature.LT, IndexFeature.GT, IndexFeature.BT},
                                                 attr -> NavigableIndex.onAttribute(dataSource, attr))

        );
    }

    @Override
    public String toString() {
        return "PostgreSQLIndexEngine[" + dataSource + "]";
    }
}
