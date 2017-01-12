/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.EntityIndex;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.logical.Or;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.greaterThan;


/**
 * IsLatestEntity filters for entities that are the most recent against a queryFunction.
 *
 * For example, to query the latest NameChanged across all entities:
 *
 * <code>
 *     isLatestEntity(repository.getIndexEngine().getIndexedCollection(NameChanged.class),
 *                    (h) -&gt; equal(NameChanged.REFERENCE_ID, h.get().reference()),
 *                    NameChanged.TIMESTAMP)
 * </code>
 *
 * @see QueryFactory#isLatestEntity(IndexedCollection, Function, EntityIndex)
 * @see QueryFactory#isLatestEntity(IndexedCollection, Query, EntityIndex)
 *
 * @param <O>
 */
public class IsLatestEntity<O extends EntityHandle> extends SimpleQuery<O, HybridTimestamp> {

    private final IndexedCollection<O> collection;
    private final Attribute<O, HybridTimestamp> timestampAttribute;
    private Function<O, Query<O>> queryFunction;
    private Query<O> query;

    /**
     * @param collection collection to query against
     * @param queryFunction query returning function
     * @param timestampAttribute timestamp attribute.
     */
    public IsLatestEntity(IndexedCollection<O> collection,
                          Function<O, Query<O>> queryFunction,
                          Attribute<O, HybridTimestamp> timestampAttribute) {
        super(timestampAttribute);
        this.collection = collection;
        this.queryFunction = queryFunction;
        this.timestampAttribute = timestampAttribute;
    }

    /**
     * @param collection collection to query against
     * @param query query
     * @param timestampAttribute timestamp attribute.
     */
    public IsLatestEntity(IndexedCollection<O> collection,
                          Query<O> query,
                          Attribute<O, HybridTimestamp> timestampAttribute) {
        super(timestampAttribute);
        this.collection = collection;
        this.query = query;
        this.timestampAttribute = timestampAttribute;
    }

    @Value
    @Accessors(fluent = true)
    private static class TerminatedRecords<O> {
        private Map<Query<O>, UUID> queries = new HashMap<>();
    }

    private Optional<Boolean> terminatedQuery(O object, Query<O> query, QueryOptions queryOptions) {
        if (queryOptions.get(TerminatedRecords.class) == null) {
            queryOptions.put(TerminatedRecords.class, new TerminatedRecords<>());
        }
        TerminatedRecords terminatedRecords = queryOptions.get(TerminatedRecords.class);
        UUID record = (UUID) terminatedRecords.queries().get(query);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(record == object.uuid());
    }

    private boolean matches(ResultSet<O> resultSet, Query<O> query, O object, QueryOptions queryOptions) {
        boolean matches = resultSet.size() == 0;
        if (matches) {
            @SuppressWarnings("unchecked")
            TerminatedRecords<O> terminatedRecords = queryOptions.get(TerminatedRecords.class);
            terminatedRecords.queries().put(query, object.uuid());
        }
        return matches;
    }

    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<O, HybridTimestamp> attribute, O object, QueryOptions
            queryOptions) {
        Query<O> actualQuery = query == null ? queryFunction.apply(object) : query;
        Optional<Boolean> terminatedQuery = terminatedQuery(object, actualQuery, queryOptions);
        if (terminatedQuery.isPresent()) {
            return terminatedQuery.get();
        }
        HybridTimestamp value = attribute.getValue(object, queryOptions);
        try (ResultSet<O> resultSet = collection.retrieve(and(
                actualQuery,
                greaterThan(timestampAttribute, value)))) {
            return matches(resultSet, actualQuery, object, queryOptions);
        }
    }

    @Override
    protected boolean matchesNonSimpleAttribute(Attribute<O, HybridTimestamp> attribute, O object, QueryOptions
            queryOptions) {
        Query<O> actualQuery = query == null ? queryFunction.apply(object) : query;
        Optional<Boolean> terminatedQuery = terminatedQuery(object, actualQuery, queryOptions);
        if (terminatedQuery.isPresent()) {
            return terminatedQuery.get();
        }
        Iterable<HybridTimestamp> values = attribute.getValues(object, queryOptions);
        List<Query<O>> conditions = StreamSupport.stream(values.spliterator(), false)
                                                 .map(v -> greaterThan(timestampAttribute, v))
                                                 .collect(Collectors.toList());
        Query<O> timestampQuery = conditions.size() == 1 ? conditions.get(0) : new Or<>(conditions);
        try (ResultSet<O> resultSet = collection.retrieve(and(
                actualQuery,
                timestampQuery))) {
            return matches(resultSet, actualQuery, object, queryOptions);
        }
    }

    @Override protected int calcHashCode() {
        int result = collection.hashCode();
        result = 31 * result + (query == null ? queryFunction : query).hashCode();
        result = 31 * result + timestampAttribute.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "isLatestEntity(" +
                "IndexedCollection<" + timestampAttribute.getObjectType().getSimpleName() + ">" +
                ", query=" + query == null ? queryFunction.toString() : query +
                ", timestamp=" + asLiteral(timestampAttribute.getAttributeName()) +
                ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IsLatestEntity)) return false;

        IsLatestEntity latestReference = (IsLatestEntity) o;

        if (!collection.equals(latestReference.collection)) return false;
        if (query != null && !query.equals(query)) return false;
        if (queryFunction != null && !queryFunction.equals(latestReference.queryFunction)) return false;
        if (!timestampAttribute.equals(latestReference.timestampAttribute)) return false;

        return true;
    }
}
