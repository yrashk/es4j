/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * An extension of {@link com.googlecode.cqengine.attribute.SimpleAttribute} that hides
 * the unnecessary complexity of using {@link EntityHandle}
 * @param <O>
 * @param <A>
 */
public abstract class SimpleAttribute<O extends Entity, A> extends com.googlecode.cqengine.attribute.SimpleAttribute<EntityHandle<O>, A> {

    public SimpleAttribute() {
    }

    public SimpleAttribute(String attributeName) {
        super(attributeName);
    }

    public SimpleAttribute(Class<EntityHandle<O>> objectType, Class<A> attributeType) {
        super(objectType, attributeType);
    }

    public SimpleAttribute(Class<EntityHandle<O>> objectType, Class<A> attributeType, String attributeName) {
        super(objectType, attributeType, attributeName);
    }

    @Override
    public A getValue(EntityHandle<O> object, QueryOptions queryOptions) {
        return getValue(object.get(), queryOptions);
    }

    public abstract A getValue(O object, QueryOptions queryOptions);
}
