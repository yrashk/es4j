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
import com.eventsourcing.index.Attribute;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.postgresql.PostgreSQLSerialization;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.io.BaseEncoding;
import com.googlecode.cqengine.index.support.KeyStatisticsAttributeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.query.simple.Has;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;

@Slf4j
public class EqualityIndex<A, O extends Entity> extends PostgreSQLAttributeIndex<A, O>
        implements KeyStatisticsAttributeIndex<A, EntityHandle<O>> {

    protected static final int INDEX_RETRIEVAL_COST = 30;
    protected static final int UNIQUE_INDEX_RETRIEVAL_COST = 25;

    @Getter
    private final DataSource dataSource;
    @Getter
    private String tableName;
    @Getter
    private Layout<O> layout;
    @Getter
    private final TypeHandler attributeTypeHandler;
    @Getter
    private final boolean unique;

    public static <A, O extends Entity> EqualityIndex<A, O> onAttribute(DataSource dataSource,
                                                                        Attribute<O, A> attribute, boolean unique) {
        return new EqualityIndex<>(dataSource, attribute, unique);
    }

    @SneakyThrows
    protected EqualityIndex(DataSource dataSource, Attribute<O, A> attribute, boolean unique) {
        super(attribute, new HashSet<Class<? extends Query>>() {{
            add(Equal.class);
            add(Has.class);
        }});
        this.dataSource = dataSource;
        this.unique = unique;
        layout = Layout.forClass(attribute.getEffectiveObjectType());
        TypeResolver typeResolver = new TypeResolver();
        ResolvedType resolvedType = typeResolver.resolve(attribute.getAttributeType());
        attributeTypeHandler = TypeHandler.lookup(resolvedType, null);
        init();
    }

    @SneakyThrows
    private void init() {
        try(Connection connection = dataSource.getConnection()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(layout.getHash());
            digest.update(attribute.getAttributeName().getBytes());
            String encodedHash = BaseEncoding.base16().encode(digest.digest());
            tableName = "index_v1_" + encodedHash + "_eq";
            if (unique) {
                tableName += "_unique";
            }
            String attributeType = PostgreSQLSerialization.getMappedType(connection, attributeTypeHandler);
            if (unique) {
                attributeType += " UNIQUE";
            }
            String create = "CREATE TABLE IF NOT EXISTS " + getTableName() + " (" +
                    "\"key\" " + attributeType + ",\n" +
                    "\"object\" UUID" +
                    ")";
            try (PreparedStatement s = connection.prepareStatement(create)) {
                s.executeUpdate();
            }
            if (!unique) {
                String indexKey = "CREATE INDEX IF NOT EXISTS " + getTableName() + "_key_idx ON " + getTableName() + " (\"key\")";
                try (PreparedStatement s = connection.prepareStatement(indexKey)) {
                    s.executeUpdate();
                }
            }
            String indexObj = "CREATE INDEX IF NOT EXISTS " + getTableName() + "_obj_idx ON " + getTableName() + " (\"object\")";
            try (PreparedStatement s = connection.prepareStatement(indexObj)) {
                s.executeUpdate();
            }
            String indexComment = layout.getName() + "." + attribute.getAttributeName() + " EQ";
            if (unique) {
                indexComment += " UNIQUE";
            }
            String comment = "COMMENT ON TABLE " + getTableName() + " IS '" + indexComment + "'";
            try (PreparedStatement s = connection.prepareStatement(comment)) {
                s.executeUpdate();
            }

        }
    }

    @Override public ResultSet<EntityHandle<O>> retrieve(Query<EntityHandle<O>> query, QueryOptions queryOptions) {
        return super.retrieve(query, queryOptions);
    }

    @Override public String toString() {
        return "EqualityIndex[PostgreSQL, table=" + getTableName() + "]";
    }

    @Override protected int indexRetrievalCost() {
        return isUnique() ? UNIQUE_INDEX_RETRIEVAL_COST : INDEX_RETRIEVAL_COST;
    }
}
