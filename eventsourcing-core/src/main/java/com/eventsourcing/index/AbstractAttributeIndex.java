/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.layout.core.Deserializer;
import com.eventsourcing.layout.core.Serializer;
import com.eventsourcing.layout.types.UnknownTypeHandler;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import lombok.SneakyThrows;

import java.beans.IntrospectionException;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public abstract class AbstractAttributeIndex<A, O> extends com.googlecode.cqengine.index.support.AbstractAttributeIndex<A, O> {

    protected Serializer<A> attributeSerializer;
    protected Deserializer<A> attributeDeserializer;
    protected Serializer<O> objectSerializer;
    protected Deserializer<O> objectDeserializer;

    /**
     * Protected constructor, called by subclasses.
     *
     * @param attribute        The attribute on which the index will be built
     * @param supportedQueries The set of {@link Query} types which the subclass implementation supports
     */
    @SneakyThrows
    protected AbstractAttributeIndex(Attribute<O, A> attribute, Set<Class<? extends Query>> supportedQueries) {
        super(attribute, supportedQueries);

        ResolvedType attributeType = new TypeResolver().resolve(attribute.getAttributeType());

        AnnotatedParameterizedType cls = (AnnotatedParameterizedType) attribute.getClass().getAnnotatedSuperclass();
        AnnotatedType annotatedType = cls.getAnnotatedActualTypeArguments()[1];

        TypeHandler<A> attrTypeHandler = TypeHandler.lookup(attributeType, annotatedType);
        attributeSerializer = attrTypeHandler;
        attributeDeserializer = attrTypeHandler;

        ResolvedType objectType = new TypeResolver().resolve(attribute.getObjectType());
        TypeHandler<O> objectTypeHandler = TypeHandler.lookup(objectType, null);
        if (!(objectTypeHandler instanceof UnknownTypeHandler)) {
            objectSerializer = objectTypeHandler;
            objectDeserializer = objectTypeHandler;
        } else {
            try {
                Layout<O> oLayout = new Layout<>(attribute.getObjectType());
                objectSerializer = new com.eventsourcing.layout.Serializer<>(oLayout);
                objectDeserializer = new com.eventsourcing.layout.Deserializer<>(oLayout);
            } catch (IntrospectionException | NoSuchAlgorithmException | IllegalAccessException | com.eventsourcing.layout.Deserializer.NoEmptyConstructorException e) {
                assert false;
                e.printStackTrace();
            }
        }
    }
}
