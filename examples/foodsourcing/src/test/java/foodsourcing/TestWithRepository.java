/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.eventsourcing.*;
import com.eventsourcing.cep.events.Deleted;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.index.CascadingIndexEngine;
import com.eventsourcing.index.IndexEngine;
import com.eventsourcing.index.MemoryIndexEngine;
import com.eventsourcing.inmem.MemoryJournal;
import com.eventsourcing.postgresql.PostgreSQLIndexEngine;
import com.eventsourcing.postgresql.PostgreSQLJournal;
import com.eventsourcing.repository.StandardRepository;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import foodsourcing.events.AddressChanged;
import foodsourcing.events.RestaurantRegistered;
import foodsourcing.events.WorkingHoursChanged;
import lombok.SneakyThrows;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TestWithRepository {

    private static HikariDataSource dataSource;
    private final Package[] packages;
    protected Repository repository;
    protected LocalLockProvider lockProvider;
    protected NTPServerTimeProvider timeProvider;
    private Journal journal;
    private IndexEngine indexEngine;

    public TestWithRepository(Package ...packages) {
        this.packages = packages;
    }


    @BeforeMethod
    public void setUp() throws Exception {
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        repository = new StandardRepository();
        repository.setPhysicalTimeProvider(timeProvider);
        journal = createJournal();
        repository.setJournal(journal);
        indexEngine = createIndexEngine();
        repository.setIndexEngine(indexEngine);
        lockProvider = new LocalLockProvider();
        repository.setLockProvider(lockProvider);
        repository.startAsync().awaitRunning();
        // Add commands/events after the startup, to simulate production better
        repository.addCommandSetProvider(new PackageCommandSetProvider(packages));
        repository.addEventSetProvider(new PackageEventSetProvider(packages));
        wipeout();
    }

    @SneakyThrows
    private void wipeout() {
        journal.clear();
        indexEngine.getIndexedCollection(AddressChanged.class).clear();
        indexEngine.getIndexedCollection(RestaurantRegistered.class).clear();
        indexEngine.getIndexedCollection(WorkingHoursChanged.class).clear();
    }

    private IndexEngine createIndexEngine() {
        if (System.getProperty("postgres") != null) {
            return new CascadingIndexEngine(new PostgreSQLIndexEngine(getPostgreSQLDataSource()),
                                            new MemoryIndexEngine());
        } else {
            return new MemoryIndexEngine();
        }
    }

    @SneakyThrows
    private static DataSource getPostgreSQLDataSource() {
        if (dataSource == null ) {
            PGDataSource ds = new PGDataSource();
            ds.setHost("localhost");
            ds.setDatabase("foodsourcing");
            ds.setUser("foodsourcing");
            ds.setPassword("foodsourcing");
            ds.setHousekeeper(false);

            HikariConfig config = new HikariConfig();
            config.setMaximumPoolSize(50);
            config.setDataSource(ds);
            config.setLeakDetectionThreshold(2000);
            config.setConnectionInitSql("SET log_statement = 'all'; SET search_path = 'public'");

            dataSource = new HikariDataSource(config);
        }

        return dataSource;
    }

    private Journal createJournal() {
        if (System.getProperty("postgres") != null) {
            return new PostgreSQLJournal(getPostgreSQLDataSource());
        } else {
            return new MemoryJournal();
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
        repository.stopAsync().awaitTerminated();
    }
}
