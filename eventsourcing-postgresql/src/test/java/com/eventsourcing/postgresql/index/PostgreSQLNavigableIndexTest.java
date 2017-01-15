/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.StandardEntity;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.NavigableIndexTest;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.layout.Layout;
import com.google.common.io.BaseEncoding;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.quantizer.Quantizer;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.eventsourcing.postgresql.PostgreSQLTest.createDataSource;
import static org.testng.Assert.fail;

@Test
public class PostgreSQLNavigableIndexTest extends NavigableIndexTest<NavigableIndex> {


    private DataSource dataSource;

    @Override
    public <A extends Comparable<A>, O extends Entity> NavigableIndex onAttribute(Attribute<O, A> attribute) {
        if (dataSource == null) {
            this.dataSource = createDataSource();
        }
        return NavigableIndex.onAttribute(dataSource, attribute);
    }

    @Override
    public <A extends Comparable<A>, O extends Entity> Index<EntityHandle<O>>
    withQuantizerOnAttribute(Quantizer<A> quantizer, com.eventsourcing.index.Attribute<O, A> attribute) {
        if (dataSource == null) {
            this.dataSource = createDataSource();
        }
        return NavigableIndex.withQuantizerOnAttribute(dataSource, quantizer, attribute);
    }

    public static class TestEntity extends StandardEntity {
        public static final SimpleAttribute<TestEntity, HybridTimestamp> TIMESTAMP = new SimpleAttribute<TestEntity, HybridTimestamp>("ts") {
            @Override public HybridTimestamp getValue(TestEntity object, QueryOptions queryOptions) {
                return object.timestamp();
            }
        };

    }

    // Because of the bug fixed in https://github.com/eventsourcing/es4j/pull/197 (commit a4d6771)
    // serializable comparable for timestamp wasn't correct and such indices have to be rebuilt
    @Test
    @SneakyThrows
    public void test_a4d6711_drop() {
        if (dataSource == null) {
            this.dataSource = createDataSource();
        }
        try (Connection c = dataSource.getConnection()) {
            Layout<TestEntity> layout = Layout.forClass(TestEntity.class);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(layout.getHash());
            digest.update("ts".getBytes());
            String encodedHash = BaseEncoding.base16().encode(digest.digest());
            String tableName = "index_v1_" + encodedHash + "_navigable";

            // this is how this index looked before a4d6771
            String create = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "\"key\" BIGINT,\n" +
                    "\"object\" UUID," +
                    "PRIMARY KEY(\"key\", \"object\")" +
                    ")";
            try (PreparedStatement s = c.prepareStatement(create)) {
                s.executeUpdate();
            }

            onAttribute(TestEntity.TIMESTAMP);

            try (PreparedStatement s = c.prepareStatement("SELECT count(column_name) from information_schema.columns where " +
                                                              "lower(table_name) = lower(?) AND lower(column_name) = " +
                                                              "'key' AND lower(data_type) = 'numeric'")) {
                s.setString(1, tableName);
                try (java.sql.ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt(1) != 1) {
                            fail("key data_type is still not numeric");
                        }
                    }
                }
            }
        }
    }

}
