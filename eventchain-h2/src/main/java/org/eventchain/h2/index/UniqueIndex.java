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
package org.eventchain.h2.index;

import com.google.common.collect.UnmodifiableIterator;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Value;
import org.eventchain.index.AbstractHashingAttributeIndex;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UniqueIndex<A, O> extends AbstractHashingAttributeIndex<A, O> {


    protected static final int INDEX_RETRIEVAL_COST = 25;

    private final MVStore store;

    /**
     * Map record structure:
     *
     * <table>
     *     <tr>
     *         <th>Key</th>
     *         <th colspan="2">Value</th>
     *     </tr>
     *     <tbody>
     *         <tr>
     *           <td>hash(attribute value)</td>
     *           <td>attribute value</td>
     *           <td>object value</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    private final MVMap<byte[], byte[]> map;

    public UniqueIndex(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
        super(attribute, new HashSet<Class<? extends Query>>() {{
            add(Equal.class);
        }}, hashFunction);
        this.store = store;
        String classname = attribute.getObjectType().getName();
        map = store.openMap("unique_index_" + classname + "_" + attribute.getAttributeName());
    }

    public static <A, O> UniqueIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute) {
        return onAttribute(store, attribute, Hashing.sha1());
    }
    public static <A, O> UniqueIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
        return new UniqueIndex<>(store, attribute, hashFunction);
    }

    @Override
    public boolean isMutable() {
        return !map.isReadOnly();
    }

    @Override
    public boolean isQuantized() {
        return false;
    }

    @Override
    public ResultSet<O> retrieve(Query<O> query, QueryOptions queryOptions) {
        Class<?> queryClass = query.getClass();
        if (queryClass.equals(Equal.class)) {
            final Equal<O, A> equal = (Equal<O, A>) query;
            byte[] val = map.get(encodeKey(equal.getValue()));
            final O obj = val == null ? null : decodeVal(val).getObject();

            return new ResultSet<O>() {
                @Override
                public Iterator<O> iterator() {
                    return new UnmodifiableIterator<O>() {
                        boolean hasNext = (obj != null);
                        @Override
                        public boolean hasNext() {
                            return this.hasNext;
                        }
                        @Override
                        public O next() {
                            this.hasNext=false;
                            return obj;
                        }
                    };
                }

                @Override
                public boolean contains(O object) {
                    return (object != null && obj != null && object.equals(obj));
                }

                @Override
                public boolean matches(O object) {
                    return query.matches(object, queryOptions);
                }

                @Override
                public Query<O> getQuery() {
                    return query;
                }

                @Override
                public QueryOptions getQueryOptions() {
                    return queryOptions;
                }

                @Override
                public int getRetrievalCost() {
                    return INDEX_RETRIEVAL_COST;
                }

                @Override
                public int getMergeCost() {
                    return obj == null ? 0 : 1;
                }

                @Override
                public int size() {
                    return obj == null ? 0 : 1;
                }

                @Override
                public void close() {

                }
            };
        }
        throw new IllegalArgumentException("Unsupported query: " + query);
    }


    @Value
    static class Entry {
        private byte[] key;
        private byte[] value;
        private byte[] attr;
        private byte[] attrHash;
    }

    private byte[] encodeKey(A value) {
        int attributeSize = attributeSerializer.size(value);
        ByteBuffer serializedAttribute = ByteBuffer.allocate(attributeSize);
        attributeSerializer.serialize(value, serializedAttribute);
        return hashFunction.hashBytes(serializedAttribute.array()).asBytes();
    }

    private Entry encodeEntry(O object, A value) {
        int attributeSize = attributeSerializer.size(value);
        ByteBuffer serializedAttribute = ByteBuffer.allocate(attributeSize);
        attributeSerializer.serialize(value, serializedAttribute);

        int objectSize = objectSerializer.size(object);
        ByteBuffer serializedObject = ByteBuffer.allocate(objectSize);
        objectSerializer.serialize(object, serializedObject);

        byte[] attrHash = hashFunction.hashBytes(serializedAttribute.array()).asBytes();

        return new Entry(attrHash,
                Bytes.concat(serializedAttribute.array(), serializedObject.array()),
                serializedAttribute.array(), attrHash);
    }

    @Value
    static class Val<A, O> {
        private A attr;
        private O object;
    }

    public Val<A, O> decodeVal(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A attr = attributeDeserializer.deserialize(buffer);
        O obj = objectDeserializer.deserialize(buffer);
        return new Val<>(attr, obj);
    }

    @Override
    public boolean addAll(Collection<O> objects, QueryOptions queryOptions) {
        for (O object : objects) {
            for (A value : attribute.getValues(object, queryOptions)) {
                if (value != null) { // Don't index null attribute values
                    Entry entry = encodeEntry(object, value);
                    if (map.containsKey(entry.getKey()) && !decodeVal(map.get(entry.getKey())).getObject().equals(object)) {
                            throw new com.googlecode.cqengine.index.unique.UniqueIndex.UniqueConstraintViolatedException(
                                    "The application has attempted to add a duplicate object to the UniqueIndex on attribute '"
                                            + attribute.getAttributeName() +
                                            "', potentially causing inconsistencies between indexes. " +
                                            "UniqueIndex should not be used with attributes which do not uniquely identify objects. " +
                                            "Problematic attribute value: '" + decodeVal(map.get(entry.getKey())).getAttr() + "', " +
                                            "problematic duplicate object: " + object);
                    }
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<O> objects, QueryOptions queryOptions) {
        for (O object : objects) {
            for (A value : attribute.getValues(object, queryOptions)) {
                Entry entry = encodeEntry(object, value);
                map.remove(entry.getKey());
            }
        }
        return true;
    }

    @Override
    public void clear(QueryOptions queryOptions) {
        map.clear();
    }

    @Override
    public void init(Set<O> collection, QueryOptions queryOptions) {
        addAll(collection, queryOptions);
    }
}
