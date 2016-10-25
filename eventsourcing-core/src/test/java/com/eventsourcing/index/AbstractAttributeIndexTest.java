/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.models.Car;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

import static org.testng.Assert.assertEquals;

public class AbstractAttributeIndexTest {

    private static class AttributeIndex<A, O extends Entity> extends AbstractAttributeIndex<A, O> {

        /**
         * Protected constructor, called by subclasses.
         *
         * @param attribute        The attribute on which the index will be built
         * @param supportedQueries The set of {@link Query} types which the subclass implementation supports
         */
        protected AttributeIndex(Attribute<O, A> attribute, Set<Class<? extends Query>> supportedQueries) {
            super(attribute, supportedQueries);
        }

        @Override
        public boolean isMutable() {
            return false;
        }

        @Override
        public boolean isQuantized() {
            return false;
        }

        @Override
        public com.googlecode.cqengine.resultset.ResultSet<EntityHandle<O>> retrieve(Query<EntityHandle<O>> query,
                                                                                     QueryOptions queryOptions) {
            return null;
        }

        @Override
        public Index<EntityHandle<O>> getEffectiveIndex() {
            return this;
        }

        @Override
        public boolean addAll(ObjectSet<EntityHandle<O>> objects, QueryOptions queryOptions) {
            return false;
        }

        @Override
        public boolean removeAll(ObjectSet<EntityHandle<O>> objects, QueryOptions queryOptions) {
            return false;
        }

        @Override
        public void clear(QueryOptions queryOptions) {

        }

        @Override
        public void init(ObjectStore<EntityHandle<O>> objectStore, QueryOptions queryOptions) {

        }

    }

    public List<String> list;

    @Test
    @SneakyThrows
    public void generics() {
        SimpleAttribute<Car, List<String>> FEATURES_LIST = new SimpleAttribute<Car, List<String>>("features") {

            @SneakyThrows
            @Override public Type getAttributeReflectedType() {
                return AbstractAttributeIndexTest.class.getField("list").getGenericType();
            }

            @Override
            public List<String> getValue(Car car, QueryOptions queryOptions) {
                return car.getFeatures();
            }
        };

        AttributeIndex index = new AttributeIndex<>(FEATURES_LIST, new HashSet<Class<? extends Query>>() {{}});

        list = Arrays.asList("Hello");
        TypeResolver typeResolver = new TypeResolver();
        ResolvedType klassType = typeResolver.resolve(getClass().getField("list").getGenericType());
        TypeHandler listTypeHandler = TypeHandler.lookup(klassType);
        ByteBuffer buffer = ByteBuffer.allocate(index.attributeSerializer.size(listTypeHandler, list));
        index.attributeSerializer.serialize(listTypeHandler, list, buffer);
        buffer.rewind();
        List<String> deserialized = (List<String>) index.attributeDeserializer.deserialize(listTypeHandler, buffer);
        assertEquals(deserialized.get(0), list.get(0));
    }
}