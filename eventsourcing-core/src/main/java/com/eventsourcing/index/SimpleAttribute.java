/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
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
