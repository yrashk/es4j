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
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.SerializableComparable;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.postgresql.PostgreSQLSerialization;
import com.eventsourcing.postgresql.PostgreSQLStatementIterator;
import com.eventsourcing.queries.ComparingQuery;
import com.eventsourcing.queries.Max;
import com.eventsourcing.queries.Min;
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
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eventsourcing.postgresql.PostgreSQLSerialization.getParameter;
import static com.eventsourcing.postgresql.PostgreSQLSerialization.setValue;

public class NavigableIndex <A extends Comparable<A>, O extends Entity> extends PostgreSQLAttributeIndex<A, O>
        implements SortedKeyStatisticsAttributeIndex<A, EntityHandle<O>> {

    protected static final int INDEX_RETRIEVAL_COST = 40;
    public static final int AGGREGATE_RETRIEVAL_COST = 25;

    @Getter
    private final DataSource dataSource;
    @Getter
    private final Layout<O> layout;
    @Getter
    private final TypeHandler attributeTypeHandler;
    private final Attribute<O, A> comparableAttribute;
    @Getter
    private String tableName;
    @Getter
    private String aggregateTableName;

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
        super(attribute instanceof SerializableComparableAttribute ? ((SerializableComparableAttribute) attribute)
                .getAttribute() : attribute, new HashSet<Class<?
                extends Query>>
                () {{
            add(Equal.class);
            add(LessThan.class);
            add(GreaterThan.class);
            add(Between.class);
            add(Has.class);
            add(Min.class);
            add(Max.class);
        }});
        comparableAttribute = attribute;
        this.dataSource = dataSource;
        layout = Layout.forClass(comparableAttribute.getEffectiveObjectType());
        TypeResolver typeResolver = new TypeResolver();
        ResolvedType resolvedType = typeResolver.resolve(comparableAttribute.getAttributeType());
        attributeTypeHandler = TypeHandler.lookup(resolvedType);
        init();
    }

    @Override protected com.googlecode.cqengine.attribute.Attribute<EntityHandle<O>, A> getOwnAttribute() {
        return comparableAttribute;
    }

    @SneakyThrows
    private void init() {
        try(Connection connection = dataSource.getConnection()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(layout.getHash());
            digest.update(getOwnAttribute().getAttributeName().getBytes());
            String encodedHash = BaseEncoding.base16().encode(digest.digest());
            tableName = "index_v1_" + encodedHash + "_navigable";
            String attributeType = PostgreSQLSerialization.getMappedType(connection, attributeTypeHandler);

            // Because of the bug fixed in https://github.com/eventsourcing/es4j/pull/197 (commit a4d6771)
            // serializable comparable for timestamp wasn't correct and such indices have to be rebuilt
            dropInvalidIndex_a4d6771(connection);

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
            String indexComment = layout.getName() + "." + getOwnAttribute().getAttributeName() + " EQ LT GT BT";
            String comment = "COMMENT ON TABLE " + tableName + " IS '" + indexComment + "'";
            try (PreparedStatement s = connection.prepareStatement(comment)) {
                s.executeUpdate();
            }
            aggregateTableName = "index_v1_" + encodedHash + "_navaggr";
            String createAggregates = "CREATE TABLE IF NOT EXISTS " + aggregateTableName + " (" +
                                      "aggregate_type VARCHAR(255) NOT NULL UNIQUE," +
                                      "object UUID," +
                                      "val " + attributeType +
                                      ")";
            try (PreparedStatement s = connection.prepareStatement(createAggregates)) {
                s.executeUpdate();
            }

        }
    }

    private void dropInvalidIndex_a4d6771(Connection connection) throws SQLException {
        if (getAttribute().getAttributeType() == HybridTimestamp.class) {
            try (PreparedStatement s = connection
                    .prepareStatement("SELECT count(column_name) from information_schema.columns where " +
                                          "lower(table_name) = lower(?) AND lower(column_name) = 'key' " +
                                          " AND lower(data_type) != 'numeric'")) {
                s.setString(1, tableName);
                try (java.sql.ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt(1) > 0) {
                            try (PreparedStatement drop = connection.prepareStatement("DROP TABLE " + tableName)) {
                                drop.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
    }

    private static class NavigableAdditionProcessor<O extends Entity, A extends Comparable>
            implements AdditionProcessor<O, A> {

        private final String aggregateTableName;
        private final TypeHandler attributeTypeHandler;
        private final DataSource dataSource;

        private A min;
        private UUID minRef;
        private A max;
        private UUID maxRef;

        @SneakyThrows
        public NavigableAdditionProcessor(String aggregateTableName, TypeHandler attributeTypeHandler,
                                          DataSource dataSource) {
            this.aggregateTableName = aggregateTableName;
            this.attributeTypeHandler = attributeTypeHandler;
            this.dataSource = dataSource;
            try (Connection c = dataSource.getConnection()) {
                String query = "SELECT aggregate_type, object, val FROM " + aggregateTableName + " WHERE " +
                               "aggregate_type IN ('min','max')";
                try (PreparedStatement s = c.prepareStatement(query)) {
                    try (java.sql.ResultSet rs = s.executeQuery()) {
                        while (rs.next()) {
                            String aggregateType = rs.getString(1);
                            if (aggregateType.contentEquals("min")) {
                                minRef = UUID.fromString(rs.getString(2));
                                AtomicInteger valPos = new AtomicInteger(3);
                                min = (A) PostgreSQLSerialization.getValue(rs, valPos, attributeTypeHandler);
                            }
                            if (aggregateType.contentEquals("max")) {
                                maxRef = UUID.fromString(rs.getString(2));
                                AtomicInteger valPos = new AtomicInteger(3);
                                max = (A) PostgreSQLSerialization.getValue(rs, valPos, attributeTypeHandler);
                            }

                        }
                    }
                }
            }
        }

        @Override public void commit() throws SQLException {
            try (Connection c = dataSource.getConnection()) {
                String insert = "INSERT INTO " + aggregateTableName + " (aggregate_type, object, val) " +
                        "VALUES (?, ?::UUID, ?) ON CONFLICT (aggregate_type) DO UPDATE SET object = ?::UUID, val = ? " +
                        "WHERE " +
                        aggregateTableName + ".aggregate_type = ?";
                try (PreparedStatement s = c.prepareStatement(insert)) {
                    if (min != null) {
                        s.setString(1, "min"); // insert
                        s.setString(6, "min"); // update
                        s.setString(2, minRef.toString()); // insert
                        s.setString(4, minRef.toString()); // update
                        PostgreSQLSerialization.setValue(c, s, 3, min, attributeTypeHandler); // insert
                        PostgreSQLSerialization.setValue(c, s, 5, min, attributeTypeHandler); // update
                        s.addBatch();
                    }
                    if (max != null) {
                        s.setString(1, "max"); // insert
                        s.setString(6, "max"); // update
                        s.setString(2, maxRef.toString()); // insert
                        s.setString(4, maxRef.toString()); // update
                        PostgreSQLSerialization.setValue(c, s, 3, max, attributeTypeHandler); // insert
                        PostgreSQLSerialization.setValue(c, s, 5, max, attributeTypeHandler); // update
                        s.addBatch();
                    }
                    s.executeBatch();
                }
            }
        }

        @Override public void accept(EntityHandle<O> handle, A a) {
            if (min == null || a.compareTo(min) < 0) {
                min = a;
                minRef = handle.uuid();
            }
            if (max == null || a.compareTo(max) > 0) {
                max = a;
                maxRef = handle.uuid();
            }
        }
    }

    @Override protected AdditionProcessor createAdditionProcessor() {
        return new NavigableAdditionProcessor<O, A>(getAggregateTableName(), getAttributeTypeHandler(),
                                                    getDataSource());
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
                setValue(connection, counter, 1, getSerializableValue(value), getAttributeTypeHandler());
                try (java.sql.ResultSet resultSet = counter.executeQuery()) {
                    resultSet.next();
                    size = resultSet.getInt(1);
                }
            }

            PreparedStatement s = connection
                    .prepareStatement("SELECT object FROM " + getTableName() + " WHERE key " + op + " " +
                                              getParameter(connection, getAttributeTypeHandler(), null));
            setValue(connection, s, 1, getSerializableValue(value), getAttributeTypeHandler());

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
                setValue(connection, counter, 1, getSerializableValue(value),
                         getAttributeTypeHandler());
                try (java.sql.ResultSet resultSet = counter.executeQuery()) {
                    resultSet.next();
                    size = resultSet.getInt(1);
                }
            }

            PreparedStatement s = connection
                    .prepareStatement("SELECT object FROM " + getTableName() + " WHERE key " + op + " " +
                                              getParameter(connection, getAttributeTypeHandler(), null));
            setValue(connection, s, 1, getSerializableValue(value), getAttributeTypeHandler());

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
                setValue(connection, counter, 1, getSerializableValue(lowerValue), getAttributeTypeHandler());
                setValue(connection, counter, 2, getSerializableValue(upperValue), getAttributeTypeHandler());

                try (java.sql.ResultSet resultSet = counter.executeQuery()) {
                    resultSet.next();
                    size = resultSet.getInt(1);
                }
            }

            PreparedStatement s = connection
                    .prepareStatement("SELECT object FROM " + getTableName() + " WHERE " +
                                              "key " + lowerOp + " " + parameter + " AND " +
                                              "key " + upperOp + " " + parameter);
            setValue(connection, s, 1, getSerializableValue(lowerValue), getAttributeTypeHandler());
            setValue(connection, s, 2, getSerializableValue(upperValue), getAttributeTypeHandler());

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
        if (queryClass.equals(Min.class)) {
            try (Connection c = getDataSource().getConnection()) {
                String q = "SELECT object FROM " + aggregateTableName + " WHERE " +
                           "aggregate_type = 'min'";
                try (PreparedStatement s = c.prepareStatement(q)) {
                    try (java.sql.ResultSet resultSet = s.executeQuery()) {
                        if (resultSet.next()) {
                            UUID uuid = UUID.fromString(resultSet.getString(1));
                            Iterator<EntityHandle<O>> iterator = Collections.singletonList(keyObjectStore.get(uuid))
                                                                            .iterator();
                            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<O, Min<O, A>>
                                    (iterator, (Min<O, A>) query, queryOptions, 1) {
                                @Override public int getRetrievalCost() {
                                    return AGGREGATE_RETRIEVAL_COST;
                                }
                            };
                            return new CloseableResultSet<>(rs, query, queryOptions);
                        } else {
                            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<O, Min<O, A>>
                                    (Collections.emptyIterator(), (Min<O, A>) query, queryOptions, 0) {
                                @Override public int getRetrievalCost() {
                                    return AGGREGATE_RETRIEVAL_COST;
                                }
                            };
                            return new CloseableResultSet<>(rs, query, queryOptions);
                        }
                    }
                }
            }
        }
        if (queryClass.equals(Max.class)) {
            try (Connection c = getDataSource().getConnection()) {
                String q = "SELECT object FROM " + aggregateTableName + " WHERE " +
                           "aggregate_type = 'max'";
                try (PreparedStatement s = c.prepareStatement(q)) {
                    try (java.sql.ResultSet resultSet = s.executeQuery()) {
                        if (resultSet.next()) {
                            UUID uuid = UUID.fromString(resultSet.getString(1));
                            Iterator<EntityHandle<O>> iterator = Collections.singletonList(keyObjectStore.get(uuid))
                                                                            .iterator();
                            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<O, Max<O, A>>
                                    (iterator, (Max<O, A>) query, queryOptions, 1) {
                                @Override public int getRetrievalCost() {
                                    return AGGREGATE_RETRIEVAL_COST;
                                }
                            };
                            return new CloseableResultSet<>(rs, query, queryOptions);
                        } else {
                            ResultSet<EntityHandle<O>> rs = new MatchingResultSet<O, Max<O, A>>
                                    (Collections.emptyIterator(), (Max<O, A>) query, queryOptions, 0) {
                                @Override public int getRetrievalCost() {
                                    return AGGREGATE_RETRIEVAL_COST;
                                }
                            };
                            return new CloseableResultSet<>(rs, query, queryOptions);
                        }                    }
                }
            }
        }
        return super.retrieve(query, queryOptions);
    }

    protected Object getSerializableValue(A value) {
        return value instanceof SerializableComparable ? ((SerializableComparable)
                         value).getSerializableComparable() : value;
    }


    @Override protected int indexRetrievalCost() {
        return INDEX_RETRIEVAL_COST;
    }

}
