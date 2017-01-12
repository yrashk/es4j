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
import com.eventsourcing.index.EntityIndex;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;

import java.util.Iterator;

public class Min<O extends Entity, A extends Comparable<A>> extends SimpleQuery<EntityHandle<O>, A>  {

    private EntityHandle<O> min = null;
    private void ensureMinIsFound(SimpleAttribute<EntityHandle<O>, A> attribute, QueryOptions queryOptions) {
        if (min == null) {
            IndexedCollection<EntityHandle<O>> collection = queryOptions.get(IndexedCollection.class);
            if (collection == null) {
                throw new RuntimeException(
                        toString() + " has to be supported by the target index or queryOptions should" +
                                " include IndexedCollection key");
            }
            Iterator<EntityHandle<O>> iterator = collection.iterator();
            A minValue = null;
            while (iterator.hasNext()) {
                EntityHandle<O> next = iterator.next();
                A value = attribute.getValue(next, queryOptions);
                if (min == null || value.compareTo(minValue) < 0) {
                    min = next;
                    minValue = value;
                }
            }
        }
    }
    private void ensureMinIsFound(Attribute<EntityHandle<O>, A> attribute, QueryOptions queryOptions) {
        if (min == null) {
            IndexedCollection<EntityHandle<O>> collection = queryOptions.get(IndexedCollection.class);
            if (collection == null) {
                throw new RuntimeException(
                        toString() + " has to be supported by the target index or queryOptions should" +
                                " include IndexedCollection key");
            }
            Iterator<EntityHandle<O>> iterator = collection.iterator();
            A minValue = null;
            while (iterator.hasNext()) {
                EntityHandle<O> next = iterator.next();
                Iterable<A> values = attribute.getValues(next, queryOptions);
                for (A value : values) {
                    if (min == null || value.compareTo(minValue) < 0) {
                        min = next;
                        minValue = value;
                    }
                }            }
        }
    }

    public Min(EntityIndex<O, A> index) {
        super(index.getAttribute());
    }

    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<EntityHandle<O>, A> attribute, EntityHandle<O> object,
                                             QueryOptions queryOptions) {
        ensureMinIsFound(attribute, queryOptions);
        return min.uuid().equals(object.uuid());
    }

    @Override
    protected boolean matchesNonSimpleAttribute(
            Attribute<EntityHandle<O>, A> attribute, EntityHandle<O> object,
            QueryOptions queryOptions) {
        ensureMinIsFound(attribute, queryOptions);
        return min.uuid().equals(object.uuid());
    }

    @Override protected int calcHashCode() {
        int result = attribute.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "min(" + asLiteral(super.getAttributeName()) + ")";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Min)) return false;

        Min min = (Min) o;

        if (!attribute.equals(min.attribute)) return false;

        return true;
    }

}
