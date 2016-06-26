/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.layout.*;
import com.eventsourcing.layout.binary.BinarySerialization;
import com.eventsourcing.layout.binary.ObjectBinarySerializer;
import com.eventsourcing.layout.binary.ObjectBinaryDeserializer;
import com.eventsourcing.layout.types.ObjectTypeHandler;
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

    protected Serializer<A, TypeHandler> attributeSerializer;
    protected Deserializer<A, TypeHandler> attributeDeserializer;
    protected ObjectSerializer<O> objectSerializer;
    protected ObjectDeserializer<O> objectDeserializer;

    private final static Serialization serialization = BinarySerialization.getInstance();

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

        TypeHandler attrTypeHandler = TypeHandler.lookup(attributeType, annotatedType);
        attributeSerializer = serialization.getSerializer(attrTypeHandler);
        attributeDeserializer = serialization.getDeserializer(attrTypeHandler);

        ResolvedType objectType = new TypeResolver().resolve(attribute.getObjectType());
        ObjectTypeHandler objectTypeHandler = (ObjectTypeHandler) TypeHandler.lookup(objectType, null);
        if (!(objectTypeHandler instanceof ObjectTypeHandler)) {
            throw new RuntimeException("Index " + attribute.getAttributeName() +
                                               " is not an object, but " + objectType.getBriefDescription());
        } else {
            objectSerializer = serialization.getSerializer(objectTypeHandler.getWrappedClass());
            objectDeserializer = serialization.getDeserializer(objectTypeHandler.getWrappedClass());
        }
    }
}
