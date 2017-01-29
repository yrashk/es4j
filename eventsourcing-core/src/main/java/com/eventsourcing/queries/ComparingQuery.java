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
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;
import lombok.Getter;

import java.util.Iterator;

public abstract class ComparingQuery<O extends Entity, A extends Comparable<A>> extends SimpleQuery<EntityHandle<O>, A> {

    private EntityHandle<O> target = null;

    protected abstract boolean isBetterValue(A value, A targetValue);

    @Override protected int calcHashCode() {
        return attribute.hashCode();
    }

    private void ensureTargetIsFound(Attribute<EntityHandle<O>, A> attribute, QueryOptions queryOptions) {
        if (target == null) {
            Iterable<EntityHandle<O>> collection = queryOptions.get(Iterable.class);
            if (collection == null) {
                throw new RuntimeException(
                        toString() + " has to be supported by the target index or queryOptions should" +
                        " include IndexedCollection key");
            }
            Iterator<EntityHandle<O>> iterator = collection.iterator();
            A targetValue = null;
            while (iterator.hasNext()) {
                EntityHandle<O> next = iterator.next();
                Iterable<A> values = attribute.getValues(next, queryOptions);
                for (A value : values) {
                    if (target == null || isBetterValue(value, targetValue)) {
                        target = next;
                        targetValue = value;
                    }
                }
            }
        }
    }

    public ComparingQuery(EntityIndex<O, A> index) {
        super(index.getAttribute());
    }

    @Override
    protected boolean matchesNonSimpleAttribute(Attribute<EntityHandle<O>, A> attribute, EntityHandle<O> object,
                                                QueryOptions queryOptions) {
        ensureTargetIsFound(attribute, queryOptions);
        return target.uuid().equals(object.uuid());
    }

    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<EntityHandle<O>, A> attribute, EntityHandle<O> object,
                                             QueryOptions queryOptions) {
        return matchesNonSimpleAttribute(attribute, object, queryOptions);
    }
}
