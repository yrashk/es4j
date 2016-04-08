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

import com.eventsourcing.EntityHandle;
import com.eventsourcing.models.Car;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.testng.Assert.assertEquals;

public class AbstractAttributeIndexTest {

    private static class AttributeIndex<A, O> extends AbstractAttributeIndex<A, O> {

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
        public com.googlecode.cqengine.resultset.ResultSet<O> retrieve(Query<O> query, QueryOptions queryOptions) {
            return null;
        }

        @Override
        public boolean addAll(Collection<O> objects, QueryOptions queryOptions) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<O> objects, QueryOptions queryOptions) {
            return false;
        }

        @Override
        public void clear(QueryOptions queryOptions) {

        }

        @Override
        public void init(Set<O> collection, QueryOptions queryOptions) {

        }
    }

    @Test
    public void generics() {
        SimpleAttribute<Car, List<String>> FEATURES_LIST = new SimpleAttribute<Car, List<String>>("features") {
            @Override
            public List<String> getValue(Car car, QueryOptions queryOptions) {
                return car.getFeatures();
            }
        };

        AttributeIndex<List<String>, EntityHandle<Car>> index = new AttributeIndex<>(FEATURES_LIST, new HashSet<Class<? extends Query>>() {{
        }});

        List<String> list = Arrays.asList("Hello");
        ByteBuffer buffer = ByteBuffer.allocate(index.attributeSerializer.size(list));
        index.attributeSerializer.serialize(list, buffer);
        buffer.rewind();
        List<String> deserialized = index.attributeDeserializer.deserialize(buffer);
        assertEquals(deserialized.get(0), list.get(0));
    }
}