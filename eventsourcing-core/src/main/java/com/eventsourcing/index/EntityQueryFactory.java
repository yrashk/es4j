/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.*;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.logical.And;
import com.googlecode.cqengine.query.logical.Not;
import com.googlecode.cqengine.query.logical.Or;
import com.googlecode.cqengine.query.option.*;
import com.googlecode.cqengine.query.simple.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Static factory for creating {@link Query} objects for {@link Entity}
 * @deprecated Use {@link com.eventsourcing.queries.QueryFactory instead}
 */
@Deprecated
public interface EntityQueryFactory {

    /**
     * Creates an {@link Equal} query which asserts that an attribute equals a certain value.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The value to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link Equal} query
     */
    @Deprecated
    static <O extends Entity, A> Equal<EntityHandle<O>, A> equal(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new Equal<>(entityIndex.getAttribute(), attributeValue);
    }

    /**
     * Creates a {@link LessThan} query which asserts that an attribute is less than or equal to an upper bound
     * (i.e. less than, inclusive).
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The upper bound to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return A {@link LessThan} query
     */
    @Deprecated
    static <O extends Entity, A extends Comparable<A>> LessThan<EntityHandle<O>, A>
            lessThanOrEqualTo(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new LessThan<>(entityIndex.getAttribute(), attributeValue, true);
    }

    /**
     * Creates a {@link LessThan} query which asserts that an attribute is less than (but not equal to) an upper
     * bound (i.e. less than, exclusive).
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The upper bound to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return A {@link LessThan} query
     */
    @Deprecated
    static <O extends Entity, A extends Comparable<A>> LessThan<EntityHandle<O>, A> 
           lessThan(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new LessThan<>(entityIndex.getAttribute(), attributeValue, false);
    }

    /**
     * Creates a {@link GreaterThan} query which asserts that an attribute is greater than or equal to a lower
     * bound (i.e. greater than, inclusive).
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The lower bound to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return A {@link GreaterThan} query
     */
    @Deprecated
    static <O extends Entity, A extends Comparable<A>> GreaterThan<EntityHandle<O>, A> 
           greaterThanOrEqualTo(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new GreaterThan<>(entityIndex.getAttribute(), attributeValue, true);
    }

    /**
     * Creates a {@link LessThan} query which asserts that an attribute is greater than (but not equal to) a lower
     * bound (i.e. greater than, exclusive).
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The lower bound to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return A {@link GreaterThan} query
     */
    @Deprecated
    static <O extends Entity, A extends Comparable<A>> GreaterThan<EntityHandle<O>, A> 
           greaterThan(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new GreaterThan<>(entityIndex.getAttribute(), attributeValue, false);
    }

    /**
     * Creates a {@link Between} query which asserts that an attribute is between a lower and an upper bound.
     *
     * @param entityIndex The index to which the query refers
     * @param lowerValue The lower bound to be asserted by the query
     * @param lowerInclusive Whether the lower bound is inclusive or not (true for "greater than or equal to")
     * @param upperValue The upper bound to be asserted by the query
     * @param upperInclusive Whether the upper bound is inclusive or not (true for "less than or equal to")
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return A {@link GreaterThan} query
     */
    @Deprecated
    static <O extends Entity, A extends Comparable<A>> Between<EntityHandle<O>, A> between(EntityIndex<O, A> entityIndex, A lowerValue, boolean lowerInclusive, A upperValue, boolean upperInclusive) {
        return new Between<>(entityIndex.getAttribute(), lowerValue, lowerInclusive, upperValue, upperInclusive);
    }

    /**
     * Creates a {@link Between} query which asserts that an attribute is between a lower and an upper bound,
     * inclusive.
     *
     * @param entityIndex The index to which the query refers
     * @param lowerValue The lower bound to be asserted by the query
     * @param upperValue The upper bound to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return A {@link GreaterThan} query
     */
    @Deprecated
    static <O extends Entity, A extends Comparable<A>> Between<EntityHandle<O>, A> between(EntityIndex<O, A> entityIndex, A lowerValue, A upperValue) {
        return new Between<>(entityIndex.getAttribute(), lowerValue, true, upperValue, true);
    }

    /**
     * <p> Creates a {@link In} query which asserts that an attribute has at least one value matching any value in a set of values.
     * <p> If the given attribute is a {@link com.googlecode.cqengine.attribute.SimpleAttribute}, this method will set a hint in the query to
     * indicate that results for the child queries will inherently be disjoint and so will not require deduplication.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValues The set of values to match
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link In} query
     */
    @Deprecated
    static <O extends Entity, A> Query<EntityHandle<O>> in(EntityIndex<O, A> entityIndex, A... attributeValues) {
        return in(entityIndex, Arrays.asList(attributeValues));
    }

    /**
     * <p> Creates a {@link In} query which asserts that an attribute has at least one value matching any value in a set of values.
     * <p> If the given attribute is a {@link com.googlecode.cqengine.attribute.SimpleAttribute}, this method will set a hint in the query to
     * indicate that results for the child queries will inherently be disjoint and so will not require deduplication.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValues TThe set of values to match
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link In} query
     */
    @Deprecated
    static <O extends Entity, A> Query<EntityHandle<O>> in(EntityIndex<O, A> entityIndex, Collection<A> attributeValues) {
        return in(entityIndex, entityIndex instanceof SimpleIndex, attributeValues);
    }

    /**
     * <p> Creates a {@link In} query which asserts that an attribute has at least one value matching any value in a set of values.
     * <p> Note that <b><u>this can result in more efficient queries</u></b> than several {@link Equal} queries "OR"ed together using other means.
     *
     * @param entityIndex The index to which the query refers
     * @param disjoint Set it to {@code true} if deduplication is not necessary because the results are disjoint. Set it to {@code false} deduplication is needed
     * @param attributeValues The set of values to match
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link In} query
     */
    @Deprecated
    static <O extends Entity, A> Query<EntityHandle<O>> in(EntityIndex<O, A> entityIndex, boolean disjoint, Collection<A> 
            attributeValues) {
        int n = attributeValues.size();
        switch (n) {
            case 0:
                return none(entityIndex.getAttribute().getEffectiveObjectType());
            case 1:
                A singleValue = attributeValues.iterator().next();
                return equal(entityIndex, singleValue);
            default:
                // Copy the values into a Set if necessary...
                Set<A> values = (attributeValues instanceof Set ? (Set<A>)attributeValues : new HashSet<A>(attributeValues));
                return new In<>(entityIndex.getAttribute(), disjoint, values);
        }
    }

    /**
     * Creates a {@link StringStartsWith} query which asserts that an attribute starts with a certain string fragment.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The value to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link StringStartsWith} query
     */
    @Deprecated
    static <O extends Entity, A extends CharSequence> StringStartsWith<EntityHandle<O>, A> startsWith(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new StringStartsWith<>(entityIndex.getAttribute(), attributeValue);
    }

    /**
     * Creates a {@link StringEndsWith} query which asserts that an attribute ends with a certain string fragment.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The value to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link StringEndsWith} query
     */
    @Deprecated
    static <O extends Entity, A extends CharSequence> StringEndsWith<EntityHandle<O>, A> endsWith(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new StringEndsWith<>(entityIndex.getAttribute(), attributeValue);
    }

    /**
     * Creates a {@link StringContains} query which asserts that an attribute contains with a certain string fragment.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The value to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link StringContains} query
     */
    @Deprecated
    static <O extends Entity, A extends CharSequence> StringContains<EntityHandle<O>, A> contains(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new StringContains<>(entityIndex.getAttribute(), attributeValue);
    }

    /**
     * Creates a {@link StringIsContainedIn} query which asserts that an attribute is contained in a certain string
     * fragment.
     *
     * @param entityIndex The index to which the query refers
     * @param attributeValue The value to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link StringStartsWith} query
     */
    @Deprecated
    static <O extends Entity, A extends CharSequence> StringIsContainedIn<EntityHandle<O>, A> isContainedIn(EntityIndex<O, A> entityIndex, A attributeValue) {
        return new StringIsContainedIn<>(entityIndex.getAttribute(), attributeValue);
    }

    /**
     * Creates a {@link StringMatchesRegex} query which asserts that an attribute's value matches a regular expression.
     * <p>
     * To accelerate {@code matchesRegex(...)} queries, add a Standing Query Index on {@code matchesRegex(...)}.
     *
     * @param entityIndex The index to which the query refers
     * @param regexPattern The regular expression pattern to be asserted by the query
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link StringStartsWith} query
     */
    @Deprecated
    static <O extends Entity, A extends CharSequence> StringMatchesRegex<EntityHandle<O>, A> matchesRegex(EntityIndex<O, A> entityIndex, Pattern regexPattern) {
        return new StringMatchesRegex<>(entityIndex.getAttribute(), regexPattern);
    }

    /**
     * Creates a {@link StringMatchesRegex} query which asserts that an attribute's value matches a regular expression.
     * <p>
     * To accelerate {@code matchesRegex(...)} queries, add a Standing Query Index on {@code matchesRegex(...)}.
     *
     * @param entityIndex The index to which the query refers
     * @param regex The regular expression to be asserted by the query (this will be compiled via {@link Pattern#compile(String)})
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link StringStartsWith} query
     */
    @Deprecated
    static <O extends Entity, A extends CharSequence> StringMatchesRegex<EntityHandle<O>, A> matchesRegex(EntityIndex<O, A> entityIndex, String regex) {
        return new StringMatchesRegex<>(entityIndex.getAttribute(), Pattern.compile(regex));
    }

    /**
     * Creates an {@link Has} query which asserts that an attribute has a value (is not null).
     * <p>
     * To accelerate {@code has(...)} queries, add a Standing Query Index on {@code has(...)}.
     * <p>
     * To assert that an attribute does <i>not</i> have a value (is null), use <code>not(has(...))</code>.
     * <p>
     * To accelerate <code>not(has(...))</code> queries, add a Standing Query Index on <code>not(has(...))</code>.
     *
     * @param entityIndex The index to which the query refers
     * @param <A> The type of the attribute
     * @param <O> The type of the object containing the attribute
     * @return An {@link Has} query
     */
    @Deprecated
    static <O extends Entity, A> Has<EntityHandle<O>, A> has(EntityIndex<O, A> entityIndex) {
        return new Has<>(entityIndex.getAttribute());
    }

    /**
     * Creates an {@link And} query, representing a logical AND on child queries, which when evaluated yields the
     * <u>set intersection</u> of the result sets from child queries.
     *
     * @param query1 The first child query to be connected via a logical AND
     * @param query2 The second child query to be connected via a logical AND
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return An {@link And} query, representing a logical AND on child queries
     */
    @Deprecated
    static <O extends Entity> And<EntityHandle<O>> and(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2);
        return new And<>(queries);
    }

    /**
     * Creates an {@link And} query, representing a logical AND on child queries, which when evaluated yields the
     * <u>set intersection</u> of the result sets from child queries.
     *
     * @param query1 The first child query to be connected via a logical AND
     * @param query2 The second child query to be connected via a logical AND
     * @param additionalQueries Additional child queries to be connected via a logical AND
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return An {@link And} query, representing a logical AND on child queries
     */
    @Deprecated
    static <O extends Entity> And<EntityHandle<O>> and(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, 
                                             Query<EntityHandle<O>>... 
            additionalQueries) {
        Collection<Query<EntityHandle<O>>> queries = new ArrayList<>(2 + additionalQueries.length);
        queries.add(query1);
        queries.add(query2);
        Collections.addAll(queries, additionalQueries);
        return new And<EntityHandle<O>>(queries);
    }

    /**
     * Creates an {@link And} query, representing a logical AND on child queries, which when evaluated yields the
     * <u>set intersection</u> of the result sets from child queries.
     *
     * @param query1 The first child query to be connected via a logical AND
     * @param query2 The second child query to be connected via a logical AND
     * @param additionalQueries Additional child queries to be connected via a logical AND
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return An {@link And} query, representing a logical AND on child queries
     */
    @Deprecated
    static <O extends Entity> And<EntityHandle<O>> and(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Collection<Query<EntityHandle<O>>> additionalQueries) {
        Collection<Query<EntityHandle<O>>> queries = new ArrayList<>(2 + additionalQueries.size());
        queries.add(query1);
        queries.add(query2);
        queries.addAll(additionalQueries);
        return new And<>(queries);
    }

    /**
     * Creates an {@link Or} query, representing a logical OR on child queries, which when evaluated yields the
     * <u>set union</u> of the result sets from child queries.
     *
     * @param query1 The first child query to be connected via a logical OR
     * @param query2 The second child query to be connected via a logical OR
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return An {@link Or} query, representing a logical OR on child queries
     */
    @Deprecated
    static <O extends Entity> Or<EntityHandle<O>> or(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2);
        return new Or<>(queries);
    }

    /**
     * Creates an {@link Or} query, representing a logical OR on child queries, which when evaluated yields the
     * <u>set union</u> of the result sets from child queries.
     *
     * @param query1 The first child query to be connected via a logical OR
     * @param query2 The second child query to be connected via a logical OR
     * @param additionalQueries Additional child queries to be connected via a logical OR
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return An {@link Or} query, representing a logical OR on child queries
     */
    @Deprecated
    static <O extends Entity> Or<EntityHandle<O>> or(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>>... additionalQueries) {
        Collection<Query<EntityHandle<O>>> queries = new ArrayList<>(2 + additionalQueries.length);
        queries.add(query1);
        queries.add(query2);
        Collections.addAll(queries, additionalQueries);
        return new Or<>(queries);
    }

    /**
     * Creates an {@link Or} query, representing a logical OR on child queries, which when evaluated yields the
     * <u>set union</u> of the result sets from child queries.
     *
     * @param query1 The first child query to be connected via a logical OR
     * @param query2 The second child query to be connected via a logical OR
     * @param additionalQueries Additional child queries to be connected via a logical OR
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return An {@link Or} query, representing a logical OR on child queries
     */
    @Deprecated
    static <O extends Entity> Or<EntityHandle<O>> or(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Collection<Query<EntityHandle<O>>> additionalQueries) {
        Collection<Query<EntityHandle<O>>> queries = new ArrayList<>(2 + additionalQueries.size());
        queries.add(query1);
        queries.add(query2);
        queries.addAll(additionalQueries);
        return new Or<>(queries);
    }

    /**
     * Creates a {@link Not} query, representing a logical negation of a child query, which when evaluated
     * yields the <u>set complement</u> of the result set from the child query.
     *
     * @param query The child query to be logically negated
     * @param <O> The type of the object containing attributes to which child queries refer
     * @return A {@link Not} query, representing a logical negation of a child query
     */
    @Deprecated
    static <O extends Entity> Not<EntityHandle<O>> not(Query<EntityHandle<O>> query) {
        return new Not<>(query);
    }

    /**
     * Creates a query supporting the equivalent of SQL <code>EXISTS</code>.
     * <p>
     * Asserts that objects in a local {@code IndexedCollection} match objects in a foreign collection,
     * based on a key attribute of local objects being equal to a key attribute of the foreign objects.
     * This query can be performed on the local collection, supplying the foreign collection and the
     * relevant attributes, as arguments to the query.
     * <p>
     * This supports the SQL equivalent of:<br>
     * <pre>
     * SELECT * From LocalCollection
     * WHERE EXISTS (
     *     SELECT * FROM ForeignCollection
     *     WHERE LocalCollection.localAttribute = ForeignCollection.foreignAttribute
     * )
     * </pre>
     *
     * @param foreignCollection The collection of foreign objects
     * @param localKeyAttribute An attribute of the local object
     * @param foreignKeyAttribute An attribute of objects in the foreign collection
     * @param <O> The type of the local object
     * @param <F> The type of the foreign objects
     * @param <A> The type of the common attributes
     * @return A query which checks if the local object matches any objects in the foreign collection based on the given
     * key attributes being equal
     */
    @Deprecated
    static <O extends Entity, F extends Entity, A> Query<EntityHandle<O>>
           existsIn(final IndexedCollection<EntityHandle<F>> foreignCollection,
                    final EntityIndex<O, A> localKeyAttribute,
                    final EntityIndex<F, A> foreignKeyAttribute) {
        return new ExistsIn<>(foreignCollection, localKeyAttribute.getAttribute(),
                              foreignKeyAttribute.getAttribute());
    }

    /**
     * Creates a query supporting the equivalent of SQL <code>EXISTS</code>,
     * with some additional restrictions on foreign objects.
     * <p>
     * Asserts that objects in a local {@code IndexedCollection} match objects in a foreign collection,
     * based on a key attribute of local objects being equal to a key attribute of the foreign objects,
     * AND objects in the foreign collection matching some additional criteria.
     * This query can be performed on the local collection, supplying the foreign collection and the
     * relevant attributes, as arguments to the query.
     * <p>
     * This supports the SQL equivalent of:<br>
     * <pre>
     * SELECT * From LocalCollection
     * WHERE EXISTS (
     *     SELECT * FROM ForeignCollection
     *     WHERE LocalCollection.localAttribute = ForeignCollection.foreignAttribute
     *         AND ([AND|OR|NOT](ForeignCollection.someOtherAttribute = x) ...)
     * )
     * </pre>
     * @param foreignCollection The collection of foreign objects
     * @param localKeyAttribute An attribute of the local object
     * @param foreignKeyAttribute An attribute of objects in the foreign collection
     * @param foreignRestrictions A query specifying additional restrictions on foreign objects
     * @param <O> The type of the local object
     * @param <F> The type of the foreign objects
     * @param <A> The type of the common attributes
     * @return A query which checks if the local object matches any objects in the foreign collection based on the given
     * key attributes being equal
     */
    @Deprecated
    static <O extends Entity, F extends Entity, A> Query<EntityHandle<O>>
           existsIn(final IndexedCollection<EntityHandle<F>> foreignCollection,
                    final EntityIndex<O, A> localKeyAttribute, final EntityIndex<F, A> foreignKeyAttribute,
                    final Query<EntityHandle<F>> foreignRestrictions) {
        return new ExistsIn<>(foreignCollection, localKeyAttribute.getAttribute(), foreignKeyAttribute.getAttribute(),
                              foreignRestrictions);
    }


    /**
     * Creates an {@link OrderByOption} query option, encapsulating the given list of {@link AttributeOrder} objects
     * which pair an attribute with a preference to sort results by that attribute in either ascending or descending
     * order.
     *
     * @param attributeOrders The list of attribute orders by which objects should be sorted
     * @param <O> The type of the object containing the attributes
     * @return An {@link OrderByOption} query option, requests results to be sorted in the given order
     */
    @Deprecated
    static <O> OrderByOption<O> orderBy(List<AttributeOrder<O>> attributeOrders) {
        return new OrderByOption<>(attributeOrders);
    }

    /**
     * Creates an {@link OrderByOption} query option, encapsulating the given list of {@link AttributeOrder} objects
     * which pair an attribute with a preference to sort results by that attribute in either ascending or descending
     * order.
     *
     * @param attributeOrders The list of attribute orders by which objects should be sorted
     * @param <O> The type of the object containing the attributes
     * @return An {@link OrderByOption} query option, requests results to be sorted in the given order
     */
    @Deprecated
    static <O> OrderByOption<O> orderBy(AttributeOrder<O>... attributeOrders) {
        return new OrderByOption<>(Arrays.asList(attributeOrders));
    }

    /**
     * Creates an {@link AttributeOrder} object which pairs an attribute with a preference to sort results by that
     * attribute in ascending order. These {@code AttributeOrder} objects can then be passed to the
     * {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} method to create a query option which
     * sorts results by the indicated attributes and ascending/descending preferences.
     *
     * @param entityIndex An attribute to sort by
     * @param <O> The type of the object containing the attributes
     * @return An {@link AttributeOrder} object, encapsulating the attribute and a preference to sort results by it
     * in ascending order
     */
    @Deprecated
    static <O extends Entity> AttributeOrder<EntityHandle<O>> ascending(EntityIndex<O, ? extends Comparable>
                                                                              entityIndex) {
        return new AttributeOrder<>(entityIndex.getAttribute(), false);
    }

    /**
     * Creates an {@link AttributeOrder} object which pairs an attribute with a preference to sort results by that
     * attribute in descending order. These {@code AttributeOrder} objects can then be passed to the
     * {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} method to create a query option which
     * sorts results by the indicated attributes and ascending/descending preferences.
     *
     * @param entityIndex An attribute to sort by
     * @param <O> The type of the object containing the attributes
     * @return An {@link AttributeOrder} object, encapsulating the attribute and a preference to sort results by it
     * in descending order
     */
    @Deprecated
    static <O extends Entity> AttributeOrder<EntityHandle<O>> descending(EntityIndex<O, ? extends Comparable>
                                                                              entityIndex) {
        return new AttributeOrder<>(entityIndex.getAttribute(), true);
    }

    /**
     * Creates a {@link DeduplicationOption} query option, encapsulating a given {@link DeduplicationStrategy}, which
     * when supplied to the query engine requests it to eliminate duplicates objects from the results returned using
     * the strategy indicated.
     *
     * @param deduplicationStrategy The deduplication strategy the query engine should use
     * @return A {@link DeduplicationOption} query option, requests duplicate objects to be eliminated from results
     */
    @Deprecated
    static DeduplicationOption deduplicate(DeduplicationStrategy deduplicationStrategy) {
        return new DeduplicationOption(deduplicationStrategy);
    }

    /**
     * Creates a {@link IsolationOption} query option, encapsulating a given {@link IsolationLevel}, which
     * when supplied to the query engine requests that level of transaction isolation.
     *
     * @param isolationLevel The transaction isolation level to request
     * @return An {@link IsolationOption} query option
     */
    @Deprecated
    static IsolationOption isolationLevel(IsolationLevel isolationLevel) {
        return new IsolationOption(isolationLevel);
    }

    /**
     * Creates an {@link ArgumentValidationOption} query option, encapsulating a given
     * {@link ArgumentValidationStrategy}, which when supplied to the query engine requests that some argument
     * validation may be disabled (or enabled) for performance or reliability reasons.
     *
     * @param strategy The argument validation strategy to request
     * @return An {@link ArgumentValidationOption} query option
     */
    @Deprecated
    static ArgumentValidationOption argumentValidation(ArgumentValidationStrategy strategy) {
        return new ArgumentValidationOption(strategy);
    }

    /**
     * A convenience method to encapsulate several objects together as {@link com.googlecode.cqengine.query.option.QueryOptions},
     * where the class of the object will become its key in the QueryOptions map.
     *
     * @param queryOptions The objects to encapsulate as QueryOptions
     * @return A {@link QueryOptions} object
     */
    @Deprecated
    static QueryOptions queryOptions(Object... queryOptions) {
        return queryOptions(Arrays.asList(queryOptions));
    }

    /**
     * A convenience method to encapsulate a collection of objects as {@link com.googlecode.cqengine.query.option.QueryOptions},
     * where the class of the object will become its key in the QueryOptions map.
     *
     * @param queryOptions The objects to encapsulate as QueryOptions
     * @return A {@link QueryOptions} object
     */
    @Deprecated
    static QueryOptions queryOptions(Collection<Object> queryOptions) {
        QueryOptions resultOptions = new QueryOptions();
        for (Object queryOption : queryOptions) {
            resultOptions.put(queryOption.getClass(), queryOption);
        }
        return resultOptions;
    }

    /**
     * A convenience method to encapsulate an empty collection of objects as
     * {@link com.googlecode.cqengine.query.option.QueryOptions}.
     *
     * @return A {@link QueryOptions} object
     */
    @Deprecated
    static QueryOptions noQueryOptions() {
        return new QueryOptions();
    }

    /**
     * Creates a {@link FlagsEnabled} object which may be added to query options.
     * This object encapsulates arbitrary "flag" objects which are said to be "enabled".
     * <p>
     * Some components such as indexes allow their default behaviour to be overridden by
     * setting flags in this way.
     *
     * @param flags Arbitrary objects which represent flags which may be interpreted by indexes etc.
     * @return A populated {@link FlagsEnabled} object which may be added to query options
     */
    @Deprecated
    static FlagsEnabled enableFlags(Object... flags) {
        FlagsEnabled result = new FlagsEnabled();
        for (Object flag: flags) {
            result.add(flag);
        }
        return result;
    }

    /**
     * Creates a {@link FlagsDisabled} object which may be added to query options.
     * This object encapsulates arbitrary "flag" objects which are said to be "disabled".
     * <p>
     * Some components such as indexes allow their default behaviour to be overridden by
     * setting flags in this way.
     *
     * @param flags Arbitrary objects which represent flags which may be interpreted by indexes etc.
     * @return A populated {@link FlagsDisabled} object which may be added to query options
     */
    @Deprecated
    static FlagsDisabled disableFlags(Object... flags) {
        FlagsDisabled result = new FlagsDisabled();
        for (Object flag: flags) {
            result.add(flag);
        }
        return result;
    }

    /**
     * Creates a {@link Thresholds} object which may be added to query options.
     * It encapsulates individual {@link Threshold} objects which are to override default values for thresholds which
     * can be set to tune query performance.
     *
     * @param thresholds Encapsulates Double values relating to thresholds to be overridden
     * @return A populated {@link Thresholds} object which may be added to query options
     */
    @Deprecated
    static Thresholds applyThresholds(Threshold... thresholds) {
        return new Thresholds(Arrays.asList(thresholds));
    }

    /**
     * Creates a {@link Threshold} object which may be added to query options.
     *
     * @param key The key of the threshold value to set
     * @param value The value to set for the threshold
     * @return A populated {@link Threshold} object encapsulating the given arguments
     */
    @Deprecated
    static Threshold threshold(Object key, Double value) {
        return new Threshold(key, value);
    }

    /**
     * Creates a {@link SelfAttribute} for the given object.
     *
     * @param objectType The type of object
     * @return a {@link SelfAttribute} for the given object
     */
    @Deprecated
    static <O> SelfAttribute<O> selfAttribute(Class<O> objectType) {
        return new SelfAttribute<>(objectType);
    }

    /**
     * Returns an {@link OrderMissingLastAttribute} which which can be used in an {@link #orderBy(AttributeOrder)}
     * clause to specify that objects which do not have values for the given delegate attribute should be returned after
     * objects which do have values for the attribute.
     * <p>
     * Essentially, this attribute can be used to order results based on whether a {@link #has(EntityIndex)} query on the
     * delegate attribute would return true or false. See documentation in {@link OrderMissingLastAttribute} for more
     * details.
     *
     * @param delegateAttribute The attribute which may or may not return values, based on which results should be
     * ordered
     * @param <O> The type of the object containing the attribute
     * @return An {@link OrderMissingLastAttribute} which orders objects with values before those without values
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    static <O> OrderMissingLastAttribute<O> missingLast(Attribute<O, ? extends Comparable> delegateAttribute) {
        return new OrderMissingLastAttribute<O>(delegateAttribute);
    }

    /**
     * Returns an {@link OrderMissingFirstAttribute} which which can be used in an {@link #orderBy(AttributeOrder)}
     * clause to specify that objects which do not have values for the given delegate attribute should be returned
     * before objects which do have values for the attribute.
     * <p>
     * Essentially, this attribute can be used to order results based on whether a {@link #has(EntityIndex)} query on
     * the
     * delegate attribute would return true or false. See documentation in {@link OrderMissingFirstAttribute} for more
     * details.
     *
     * @param delegateAttribute The attribute which may or may not return values, based on which results should be
     * ordered
     * @param <O> The type of the object containing the attribute
     * @return An {@link OrderMissingFirstAttribute} which orders objects without values before those with values
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    static <O> OrderMissingFirstAttribute<O> missingFirst(Attribute<O, ? extends Comparable> delegateAttribute) {
        return new OrderMissingFirstAttribute<O>(delegateAttribute);
    }

    /**
     * Creates a {@link StandingQueryAttribute} based on the given query. An index can then be built on this attribute,
     * and it will be able to to answer the query in constant time complexity O(1).
     *
     * @param standingQuery The standing query to encapsulate
     * @return a {@link StandingQueryAttribute} encapsulating the given query
     */
    @Deprecated
    static <O extends Entity> StandingQueryAttribute<EntityHandle<O>> forStandingQuery(Query<EntityHandle<O>>
                                                                                          standingQuery) {
        return new StandingQueryAttribute<>(standingQuery);
    }

    /**
     * Creates a {@link StandingQueryAttribute} which returns true if the given attribute does not have values for
     * an object.
     * <p>
     * An index can then be built on this attribute, and it will be able to to answer a <code>not(has(attribute))</code>
     * query, returning objects which do not have values for that attribute, in constant time complexity O(1).
     *
     * @param entityIndex The index which will be used in a <code>not(has(attribute))</code> query
     * @return a {@link StandingQueryAttribute} which returns true if the given attribute does not have values for
     * an object
     */
    @Deprecated
    static <O extends Entity, A> StandingQueryAttribute<EntityHandle<O>> forObjectsMissing(EntityIndex<O, A>
                                                                                                 entityIndex) {
        return forStandingQuery(not(has(entityIndex)));
    }

    // ***************************************************************************************************************
    // The following methods are just overloaded vararg variants of existing methods above.
    // These methods are unnecessary as of Java 7, and are provided only for backward compatibility with Java 6 and
    // earlier.
    //
    // The methods exists to work around warnings output by the Java compiler for *client code* invoking generic
    // vararg methods: "unchecked generic array creation of type Query<Foo>[] for varargs parameter" and similar.
    // See http://mail.openjdk.java.net/pipermail/coin-dev/2009-March/000217.html - project coin feature
    // in Java 7 which addresses the issue.
    // ***************************************************************************************************************

    /**
     * Overloaded variant of {@link #and(Query, Query, Query[])} - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O extends Entity> And<EntityHandle<O>> and(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>> query3) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2, query3);
        return new And<EntityHandle<O>>(queries);
    }

    /**
     * Overloaded variant of {@link #and(Query, Query, Query[])} - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O extends Entity> And<EntityHandle<O>> and(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>> query3, Query<EntityHandle<O>> query4) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2, query3, query4);
        return new And<EntityHandle<O>>(queries);
    }

    /**
     * Overloaded variant of {@link #and(Query, Query, Query[])} - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O extends Entity> And<EntityHandle<O>> and(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>> query3, Query<EntityHandle<O>> query4, Query<EntityHandle<O>> query5) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2, query3, query4, query5);
        return new And<EntityHandle<O>>(queries);
    }

    // ***************************************************************************************************************

    /**
     * Overloaded variant of {@link #or(Query, Query, Query[])} - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O extends Entity> Or<EntityHandle<O>> or(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>> query3) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2, query3);
        return new Or<>(queries);
    }

    /**
     * Overloaded variant of {@link #or(Query, Query, Query[])} - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O extends Entity> Or<EntityHandle<O>> or(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>> query3, Query<EntityHandle<O>> query4) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2, query3, query4);
        return new Or<>(queries);
    }

    /**
     * Overloaded variant of {@link #or(Query, Query, Query[])} - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O extends Entity> Or<EntityHandle<O>> or(Query<EntityHandle<O>> query1, Query<EntityHandle<O>> query2, Query<EntityHandle<O>> query3, Query<EntityHandle<O>> query4, Query<EntityHandle<O>> query5) {
        @SuppressWarnings({"unchecked"})
        Collection<Query<EntityHandle<O>>> queries = Arrays.asList(query1, query2, query3, query4, query5);
        return new Or<>(queries);
    }

    // ***************************************************************************************************************

    /**
     * Overloaded variant of {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} - see that method
     * for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O> OrderByOption<O> orderBy(AttributeOrder<O> attributeOrder) {
        @SuppressWarnings({"unchecked"})
        List<AttributeOrder<O>> attributeOrders = Collections.singletonList(attributeOrder);
        return new OrderByOption<O>(attributeOrders);
    }

    /**
     * Overloaded variant of {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} - see that method
     * for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O> OrderByOption<O> orderBy(AttributeOrder<O> attributeOrder1, AttributeOrder<O> attributeOrder2) {
        @SuppressWarnings({"unchecked"})
        List<AttributeOrder<O>> attributeOrders = Arrays.asList(attributeOrder1, attributeOrder2);
        return new OrderByOption<O>(attributeOrders);
    }

    /**
     * Overloaded variant of {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} - see that method
     * for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O> OrderByOption<O> orderBy(AttributeOrder<O> attributeOrder1, AttributeOrder<O> attributeOrder2,
                                               AttributeOrder<O> attributeOrder3) {
        @SuppressWarnings({"unchecked"})
        List<AttributeOrder<O>> attributeOrders = Arrays.asList(attributeOrder1, attributeOrder2, attributeOrder3);
        return new OrderByOption<O>(attributeOrders);
    }

    /**
     * Overloaded variant of {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} - see that method
     * for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O> OrderByOption<O> orderBy(AttributeOrder<O> attributeOrder1, AttributeOrder<O> attributeOrder2,
                                               AttributeOrder<O> attributeOrder3, AttributeOrder<O> attributeOrder4) {
        @SuppressWarnings({"unchecked"})
        List<AttributeOrder<O>> attributeOrders = Arrays.asList(attributeOrder1, attributeOrder2, attributeOrder3,
                                                                attributeOrder4);
        return new OrderByOption<O>(attributeOrders);
    }

    /**
     * Overloaded variant of {@link #orderBy(com.googlecode.cqengine.query.option.AttributeOrder[])} - see that method
     * for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static <O> OrderByOption<O> orderBy(AttributeOrder<O> attributeOrder1, AttributeOrder<O> attributeOrder2,
                                               AttributeOrder<O> attributeOrder3, AttributeOrder<O> attributeOrder4,
                                               AttributeOrder<O> attributeOrder5) {
        @SuppressWarnings({"unchecked"})
        List<AttributeOrder<O>> attributeOrders = Arrays.asList(attributeOrder1, attributeOrder2, attributeOrder3,
                                                                attributeOrder4, attributeOrder5);
        return new OrderByOption<O>(attributeOrders);
    }

    // ***************************************************************************************************************

    /**
     * Overloaded variant of {@link #queryOptions(Object...)}  - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static QueryOptions queryOptions(Object queryOption) {
        return queryOptions(Collections.singleton(queryOption));
    }

    /**
     * Overloaded variant of {@link #queryOptions(Object...)}  - see that method for details.
     * <p>
     * Note: This method is unnecessary as of Java 7, and is provided only for backward compatibility with Java 6 and
     * earlier, to eliminate generic array creation warnings output by the compiler in those versions.
     */
    @Deprecated
    @SuppressWarnings({"JavaDoc"})
    static QueryOptions queryOptions(Object queryOption1, Object queryOption2) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        List<Object> queryOptions = Arrays.asList(queryOption1, queryOption2);
        return queryOptions(queryOptions);
    }

    @Deprecated
    class All<O extends Entity> extends SimpleQuery<EntityHandle<O>, O> {

        final Class<O> attributeType;

        public All(Class<O> attributeType) {
            super(new Attribute<EntityHandle<O>, O>() {
                @Override
                public Class<EntityHandle<O>> getObjectType() {
                    return null;
                }

                @Override
                public Class<O> getAttributeType() {
                    return attributeType;
                }

                @Override
                public String getAttributeName() {
                    return "true";
                }

                @Override
                public Iterable<O> getValues(EntityHandle<O> object, QueryOptions queryOptions) {
                    return Collections.singletonList(object.get());
                }
            });
            this.attributeType = attributeType;
        }


        @Override
        protected boolean matchesSimpleAttribute(
                com.googlecode.cqengine.attribute.SimpleAttribute<EntityHandle<O>, O> attribute, EntityHandle<O> object,
                QueryOptions queryOptions) {
            return true;
        }

        @Override
        protected boolean matchesNonSimpleAttribute(Attribute<EntityHandle<O>, O> attribute, EntityHandle<O> object,
                                                    QueryOptions queryOptions) {
            return true;
        }

        @Override
        protected int calcHashCode() {
            return 38664811; // chosen randomly
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof All)) return false;
            All that = (All) o;
            return this.attributeType.equals(that.attributeType);
        }

        @Override
        public String toString() {
            return "all(" + super.getAttribute().getAttributeType().getSimpleName() + ".class)";
        }
    }

    /**
     * Creates a query which matches all objects in the collection.
     * <p>
     * <p>
     * This is equivalent to a literal boolean 'true'.
     *
     * @param <O> The type of the objects in the collection
     * @return A query which matches all objects in the collection
     */
    @Deprecated
    static <O extends Entity> Query<EntityHandle<O>> all(Class<O> objectType) {
        return new All<>(objectType);
    }

    @Deprecated
    class None<O extends Entity> extends SimpleQuery<EntityHandle<O>, O> {

        final Class<O> attributeType;

        public None(Class<O> attributeType) {
            super(new Attribute<EntityHandle<O>, O>() {
                @Override
                public Class<EntityHandle<O>> getObjectType() {
                    return null;
                }

                @Override
                public Class<O> getAttributeType() {
                    return attributeType;
                }

                @Override
                public String getAttributeName() {
                    return "true";
                }

                @Override
                public Iterable<O> getValues(EntityHandle<O> object, QueryOptions queryOptions) {
                    return Collections.singletonList(object.get());
                }
            });
            this.attributeType = attributeType;
        }


        @Override
        protected boolean matchesSimpleAttribute(
                com.googlecode.cqengine.attribute.SimpleAttribute<EntityHandle<O>, O> attribute, EntityHandle<O> object,
                QueryOptions queryOptions) {
            return true;
        }

        @Override
        protected boolean matchesNonSimpleAttribute(Attribute<EntityHandle<O>, O> attribute, EntityHandle<O> object,
                                                    QueryOptions queryOptions) {
            return true;
        }

        @Override
        protected int calcHashCode() {
            return 1357656690; // chosen randomly
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof None)) return false;
            None that = (None) o;
            return this.attributeType.equals(that.attributeType);
        }

        @Override
        public String toString() {
            return "none(" + super.getAttribute().getAttributeType().getSimpleName() + ".class)";
        }
    }

    /**
     * Creates a query which matches none of the objects in the collection.
     * <p>
     * <p>
     * This is equivalent to a literal boolean 'false'.
     *
     * @param <O> The type of the objects in the collection
     * @return A query which matches none of the objects in the collection
     */
    @Deprecated
    static <O extends Entity> Query<EntityHandle<O>> none(Class<O> objectType) {
        return new None<>(objectType);
    }

}
