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
import com.eventsourcing.index.CQIndexEngine;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.postgresql.index.EqualityIndex;
import com.googlecode.cqengine.attribute.Attribute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Component(property = {"type=PostgreSQLIndexEngine"})
public class PostgreSQLIndexEngine extends CQIndexEngine implements IndexEngine {

    @Reference
    protected DataSourceProvider dataSourceProvider;

    private DataSource dataSource;

    public PostgreSQLIndexEngine() {}
    public PostgreSQLIndexEngine(DataSource dataSource) {this.dataSource = dataSource;}

    @Activate
    protected void activate() {
        dataSource = dataSourceProvider.getDataSource();
    }

    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
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
                                                 attribute -> EqualityIndex.onAttribute(dataSource, attribute, true))
        );
    }

    @Override
    public String toString() {
        return "PostgreSQLIndexEngine[" + dataSource + "]";
    }
}
