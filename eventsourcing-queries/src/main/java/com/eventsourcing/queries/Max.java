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
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;

import java.util.Iterator;

public class Max<O extends Entity, A extends Comparable<A>> extends SimpleQuery<EntityHandle<O>, A>  {

    private EntityHandle<O> max = null;
    private void ensureMaxIsFound(SimpleAttribute<EntityHandle<O>, A> attribute, QueryOptions queryOptions) {
        if (max == null) {
            IndexedCollection<EntityHandle<O>> collection = queryOptions.get(IndexedCollection.class);
            if (collection == null) {
                throw new RuntimeException(
                        toString() + " has to be supported by the target index or queryOptions should" +
                                " include IndexedCollection key");
            }
            Iterator<EntityHandle<O>> iterator = collection.iterator();
            A maxValue = null;
            while (iterator.hasNext()) {
                EntityHandle<O> next = iterator.next();
                A value = attribute.getValue(next, queryOptions);
                if (max == null || value.compareTo(maxValue) > 0) {
                    max = next;
                    maxValue = value;
                }
            }
        }
    }
    private void ensureMaxIsFound(Attribute<EntityHandle<O>, A> attribute, QueryOptions queryOptions) {
        if (max == null) {
            IndexedCollection<EntityHandle<O>> collection = queryOptions.get(IndexedCollection.class);
            if (collection == null) {
                throw new RuntimeException(
                        toString() + " has to be supported by the target index or queryOptions should" +
                                " include IndexedCollection key");
            }
            Iterator<EntityHandle<O>> iterator = collection.iterator();
            A maxValue = null;
            while (iterator.hasNext()) {
                EntityHandle<O> next = iterator.next();
                Iterable<A> values = attribute.getValues(next, queryOptions);
                for (A value : values) {
                    if (max == null || value.compareTo(maxValue) > 0) {
                        max = next;
                        maxValue = value;
                    }
                }            }
        }
    }

    public Max(Attribute<EntityHandle<O>, A> attribute) {
        super(attribute);
    }
    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<EntityHandle<O>, A> attribute, EntityHandle<O> object,
                                             QueryOptions queryOptions) {
        ensureMaxIsFound(attribute, queryOptions);
        return max.uuid().equals(object.uuid());
    }

    @Override
    protected boolean matchesNonSimpleAttribute(
            Attribute<EntityHandle<O>, A> attribute, EntityHandle<O> object,
            QueryOptions queryOptions) {
        ensureMaxIsFound(attribute, queryOptions);
        return max.uuid().equals(object.uuid());
    }

    @Override protected int calcHashCode() {
        int result = attribute.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "max(" + asLiteral(super.getAttributeName()) + ")";
    }


}
