/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;


import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;
import lombok.Getter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Scoped<O extends Entity> extends SimpleQuery<EntityHandle<O>, Boolean> {

    @Getter
    private final Query<EntityHandle<O>> scope;

    @Getter
    private final Query<EntityHandle<O>> query;

    public Scoped(Query<EntityHandle<O>> scope, Query<EntityHandle<O>> query) {
        super(new SimpleAttribute<EntityHandle<O>, Boolean>() {
            @Override public Boolean getValue(EntityHandle<O> object, QueryOptions queryOptions) {
                return false;
            }
        });
        this.scope = scope;
        this.query = query;
    }

    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<EntityHandle<O>, Boolean> attribute, EntityHandle<O>
            object,
                                             QueryOptions queryOptions) {
        return matchesNonSimpleAttribute(attribute, object, queryOptions);
    }

    @Override
    protected boolean matchesNonSimpleAttribute(Attribute<EntityHandle<O>, Boolean> attribute, EntityHandle<O> object,
                                                QueryOptions queryOptions) {
        if (!scope.matches(object, queryOptions)) {
            return false;
        }
        Iterable<EntityHandle<O>> iterable = queryOptions.get(Iterable.class);
        Map<Object, Object> options = new HashMap<>(queryOptions.getOptions());
        options.put(Iterable.class, new FilteringIterable<>(scope, iterable, queryOptions));
        return query.matches(object, new QueryOptions(options));
    }

    @Override protected int calcHashCode() {
        return scope.hashCode() + 31 * query.hashCode();
    }


    private static class FilteringIterable<O extends Entity> implements Iterable<EntityHandle<O>> {
        private final Query<EntityHandle<O>> scope;
        private final Iterable<EntityHandle<O>> iterable;
        private final QueryOptions queryOptions;

        public FilteringIterable(
                Query<EntityHandle<O>> scope, Iterable<EntityHandle<O>> iterable, QueryOptions queryOptions) {
            this.scope = scope;
            this.iterable = iterable;
            this.queryOptions = queryOptions;
        }


        @Override public Iterator<EntityHandle<O>> iterator() {
            return new FilteringIterator<>(this, scope, iterable, queryOptions);
        }

        private static class FilteringIterator<O extends Entity> implements Iterator<EntityHandle<O>> {

            private final Query<EntityHandle<O>> scope;
            private final Iterable<EntityHandle<O>> iterable;
            private final QueryOptions queryOptions;
            private Iterator<EntityHandle<O>> iterator;
            private EntityHandle<O> next;

            public FilteringIterator(FilteringIterable<O> filteringIterable, Query<EntityHandle<O>> scope,
                                     Iterable<EntityHandle<O>> iterable, QueryOptions queryOptions) {
                this.scope = scope;
                this.iterable = iterable;
                this.queryOptions = new QueryOptions(new HashMap<>(queryOptions.getOptions()));
                this.queryOptions.put(Iterable.class, filteringIterable);
            }

            private void prepareIterator() {
                if (iterator == null) {
                    iterator = iterable.iterator();
                }
            }

            @Override public boolean hasNext() {
                prepareIterator();
                if (!iterator.hasNext()) {
                    next = null;
                    return false;
                }
                do {
                    next = iterator.next();
                    if (scope.matches(next, queryOptions)) {
                        return true;
                    }
                } while (iterator.hasNext());
                next = null;
                return false;
            }

            @Override public EntityHandle<O> next() {
                prepareIterator();
                if (next == null && !hasNext()) {
                    throw new NoSuchElementException();
                }
                EntityHandle<O> result = next;
                next = null;
                return result;
            }
        }
    }
}
