/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.StandardEntity;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;

import java.util.Collections;

public class EntityQueryFactory {

    public static class All<O extends StandardEntity> extends SimpleQuery<EntityHandle<O>, O> {

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
            return 3866481; // chosen randomly
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
    public static <O extends StandardEntity> Query<EntityHandle<O>> all(Class<O> objectType) {
        return new All<>(objectType);
    }

}
