/**
 * Copyright 2016 Eventchain team
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
package org.eventchain.index;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import org.eventchain.layout.Deserializer;
import org.eventchain.layout.Layout;
import org.eventchain.layout.Serializer;
import org.eventchain.layout.TypeHandler;
import org.eventchain.layout.types.UnknownTypeHandler;

import java.beans.IntrospectionException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public abstract class AbstractAttributeIndex<A, O> extends com.googlecode.cqengine.index.support.AbstractAttributeIndex<A, O> {

    protected org.eventchain.layout.core.Serializer<A> attributeSerializer;
    protected org.eventchain.layout.core.Deserializer<A> attributeDeserializer;
    protected org.eventchain.layout.core.Serializer<O> objectSerializer;
    protected org.eventchain.layout.core.Deserializer<O> objectDeserializer;

    /**
     * Protected constructor, called by subclasses.
     *
     * @param attribute        The attribute on which the index will be built
     * @param supportedQueries The set of {@link Query} types which the subclass implementation supports
     */
    protected AbstractAttributeIndex(Attribute<O, A> attribute, Set<Class<? extends Query>> supportedQueries) {
        super(attribute, supportedQueries);

        ResolvedType attributeType = new TypeResolver().resolve(attribute.getAttributeType());
        attributeSerializer = TypeHandler.lookup(attributeType);
        attributeDeserializer = TypeHandler.lookup(attributeType);

        ResolvedType objectType = new TypeResolver().resolve(attribute.getObjectType());
        TypeHandler<O> objectTypeHandler = TypeHandler.lookup(objectType);
        if (!(objectTypeHandler instanceof UnknownTypeHandler)) {
            objectSerializer = objectTypeHandler;
            objectDeserializer = objectTypeHandler;
        } else {
            try {
                Layout<O> oLayout = new Layout<>(attribute.getObjectType());
                objectSerializer = new Serializer<>(oLayout);
                objectDeserializer = new Deserializer<>(oLayout);
            } catch (IntrospectionException | NoSuchAlgorithmException | IllegalAccessException | Deserializer.NoEmptyConstructorException e) {
                assert false;
                e.printStackTrace();
            }
        }
    }
}
