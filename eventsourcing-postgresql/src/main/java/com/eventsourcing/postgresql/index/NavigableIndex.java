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
import com.eventsourcing.postgresql.PostgreSQLStatementIterator;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.io.BaseEncoding;
import com.googlecode.cqengine.index.support.SortedKeyStatisticsAttributeIndex;
import com.googlecode.cqengine.quantizer.Quantizer;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.*;
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.resultset.closeable.CloseableResultSet;
import com.googlecode.cqengine.resultset.filter.QuantizedResultSet;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.UUID;

import static com.eventsourcing.postgresql.PostgreSQLSerialization.getParameter;
import static com.eventsourcing.postgresql.PostgreSQLSerialization.setValue;

public class NavigableIndex <A extends Comparable<A>, O extends Entity> extends PostgreSQLAttributeIndex<A, O>
        implements SortedKeyStatisticsAttributeIndex<A, EntityHandle<O>> {

    protected static final int INDEX_RETRIEVAL_COST = 40;

    @Getter
    private final DataSource dataSource;
    @Getter
    private final Layout<O> layout;
    @Getter
    private final TypeHandler attributeTypeHandler;
    @Getter
    private String tableName;

    @Override protected boolean isUnique() {
        return false;
    }

    public static <A extends Comparable<A>, O extends Entity> NavigableIndex<A, O> onAttribute(DataSource dataSource,
                                                                                               Attribute<O, A> attribute) {
        return new NavigableIndex<>(dataSource, (Attribute<O, A>) serializableComparable(attribute));
    }

    public static <A extends Comparable<A>, O extends Entity> NavigableIndex<A, O>
           withQuantizerOnAttribute(DataSource dataSource, Quantizer<A> quantizer, Attribute<O, A> attribute) {
        return new NavigableIndex<A, O>(dataSource, (Attribute<O, A>) serializableComparable(attribute)) {
            @Override public boolean isQuantized() {
                return true;
            }

            @Override protected A getQuantizedValue(A attributeValue) {
                return quantizer.getQuantizedValue(attributeValue);
            }

            @Override
            public ResultSet<EntityHandle<O>> retrieve(Query<EntityHandle<O>> query, QueryOptions queryOptions) {
                ResultSet<EntityHandle<O>> rs = super.retrieve(query, queryOptions);
                return new QuantizedResultSet<>(rs, query, queryOptions);
            }
        };
    }

    @SneakyThrows
    protected NavigableIndex(DataSource dataSource, Attribute<O, A> attribute) {
        super(attribute, new HashSet<Class<? extends Query>>() {{
            add(Equal.class);
            add(LessThan.class);
            add(GreaterThan.class);
            add(Between.class);
            add(Has.class);
        }});
        this.dataSource = dataSource;
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
            tableName = "index_v1_" + encodedHash + "_navigable";
            String attributeType = PostgreSQLSerialization.getMappedType(connection, attributeTypeHandler);
            String create = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "\"key\" " + attributeType + ",\n" +
                    "\"object\" UUID," +
                    "PRIMARY KEY(\"key\", \"object\")" +
                    ")";
            try (PreparedStatement s = connection.prepareStatement(create)) {
                s.executeUpdate();
            }
            String indexKey = "CREATE INDEX IF NOT EXISTS " + tableName + "_key_idx ON " + tableName + " (\"key\")";
            try (PreparedStatement s = connection.prepareStatement(indexKey)) {
                s.executeUpdate();
            }
            String indexObj = "CREATE INDEX IF NOT EXISTS " + tableName + "_obj_idx ON " + tableName + " (\"object\")";
            try (PreparedStatement s = connection.prepareStatement(indexObj)) {
                s.executeUpdate();
            }
            String indexComment = layout.getName() + "." + attribute.getAttributeName() + " EQ LT GT BT";
            String comment = "COMMENT ON TABLE " + tableName + " IS '" + indexComment + "'";
            try (PreparedStatement s = connection.prepareStatement(comment)) {
                s.executeUpdate();
            }

        }
    }

    @SneakyThrows
    @Override public ResultSet<EntityHandle<O>> retrieve(Query<EntityHandle<O>> query, QueryOptions queryOptions) {
        Class<?> queryClass = query.getClass();
        if (queryClass.equals(LessThan.class)) {
            final LessThan<EntityHandle<O>, A> lessThan = (LessThan<EntityHandle<O>, A>) query;
            Connection connection = getDataSource().getConnection();

            String op = lessThan.isValueInclusive() || isQuantized() ? "<=" : "<";

            int size = 0;
            A value = getQuantizedValue(((LessThan<EntityHandle<O>, A>) query).getValue());
            try(PreparedStatement counter = connection
                    .prepareStatement("SELECT count(object) FROM " + getTableName() + " WHERE key " + op + " " +
                                      getParameter
                            (connection, getAttributeTypeHandler(), null))) {
                setValue(connection, counter, 1, value, getAttributeTypeHandler());
                try (java.sql.ResultSet resultSet = counter.executeQuery()) {
                    resultSet.next();
                    size = resultSet.getInt(1);
                }
            }

            PreparedStatement s = connection
                    .prepareStatement("SELECT object FROM " + getTableName() + " WHERE key " + op + " " +
                                              getParameter(connection, getAttributeTypeHandler(), null));
            setValue(connection, s, 1, value, getAttributeTypeHandler());

            PostgreSQLStatementIterator<EntityHandle<O>> iterator = new PostgreSQLStatementIterator<EntityHandle<O>>
                    (s, connection, isMutable()) {
                @SneakyThrows
                @Override public EntityHandle<O> fetchNext() {
                    UUID uuid = UUID.fromString(resultSet.getString(1));
                    return keyObjectStore.get(uuid);
                }
            };


            int finalSize = size;
            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<>(iterator, lessThan, queryOptions, finalSize);
            return new CloseableResultSet<>(rs, query, queryOptions);
        }
        if (queryClass.equals(GreaterThan.class)) {
            final GreaterThan<EntityHandle<O>, A> greaterThan = (GreaterThan<EntityHandle<O>, A>) query;
            Connection connection = getDataSource().getConnection();

            String op = greaterThan.isValueInclusive() || isQuantized() ? ">=" : ">";

            int size = 0;
            A value = getQuantizedValue(((GreaterThan<EntityHandle<O>, A>) query).getValue());

            try(PreparedStatement counter = connection
                    .prepareStatement("SELECT count(object) FROM " + getTableName() + " WHERE key " + op + " " +
                                              getParameter
                                                      (connection, getAttributeTypeHandler(), null))) {
                setValue(connection, counter, 1, value,
                         getAttributeTypeHandler());
                try (java.sql.ResultSet resultSet = counter.executeQuery()) {
                    resultSet.next();
                    size = resultSet.getInt(1);
                }
            }

            PreparedStatement s = connection
                    .prepareStatement("SELECT object FROM " + getTableName() + " WHERE key " + op + " " +
                                              getParameter(connection, getAttributeTypeHandler(), null));
            setValue(connection, s, 1, value, getAttributeTypeHandler());

            PostgreSQLStatementIterator<EntityHandle<O>> iterator = new PostgreSQLStatementIterator<EntityHandle<O>>
                    (s, connection, isMutable()) {
                @SneakyThrows
                @Override public EntityHandle<O> fetchNext() {
                    UUID uuid = UUID.fromString(resultSet.getString(1));
                    return keyObjectStore.get(uuid);
                }
            };


            int finalSize = size;
            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<>(iterator, greaterThan, queryOptions, finalSize);
            return new CloseableResultSet<>(rs, query, queryOptions);
        }
        if (queryClass.equals(Between.class)) {
            final Between<EntityHandle<O>, A> between = (Between<EntityHandle<O>, A>) query;
            Connection connection = getDataSource().getConnection();

            String lowerOp = between.isLowerInclusive() || isQuantized() ? ">=" : ">";
            String upperOp = between.isUpperInclusive() || isQuantized() ? "<=" : "<";

            int size = 0;
            A lowerValue = getQuantizedValue(((Between<EntityHandle<O>, A>) query).getLowerValue());
            A upperValue = getQuantizedValue(((Between<EntityHandle<O>, A>) query).getUpperValue());

            String parameter = getParameter(connection, getAttributeTypeHandler(), null);
            try(PreparedStatement counter = connection
                    .prepareStatement("SELECT count(object) FROM " + getTableName() + " WHERE " +
                                              "key " + lowerOp + " " + parameter + " AND " +
                                              "key " + upperOp + " " + parameter
                    )) {
                setValue(connection, counter, 1, lowerValue, getAttributeTypeHandler());
                setValue(connection, counter, 2, upperValue, getAttributeTypeHandler());

                try (java.sql.ResultSet resultSet = counter.executeQuery()) {
                    resultSet.next();
                    size = resultSet.getInt(1);
                }
            }

            PreparedStatement s = connection
                    .prepareStatement("SELECT object FROM " + getTableName() + " WHERE " +
                                              "key " + lowerOp + " " + parameter + " AND " +
                                              "key " + upperOp + " " + parameter);
            setValue(connection, s, 1, lowerValue, getAttributeTypeHandler());
            setValue(connection, s, 2, upperValue, getAttributeTypeHandler());

            PostgreSQLStatementIterator<EntityHandle<O>> iterator = new PostgreSQLStatementIterator<EntityHandle<O>>
                    (s, connection, isMutable()) {
                @SneakyThrows
                @Override public EntityHandle<O> fetchNext() {
                    UUID uuid = UUID.fromString(resultSet.getString(1));
                    return keyObjectStore.get(uuid);
                }
            };


            int finalSize = size;
            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<O, Between<EntityHandle<O>, A>>
                    (iterator, between, queryOptions, finalSize) {
                @Override public int getMergeCost() {
                    return finalSize;
                }
            };
            return new CloseableResultSet<>(rs, query, queryOptions);
        }
        return super.retrieve(query, queryOptions);
    }


    @Override protected int indexRetrievalCost() {
        return INDEX_RETRIEVAL_COST;
    }

}
