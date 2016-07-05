/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.h2.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.index.AbstractHashingAttributeIndex;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.repository.ResolvedEntityHandle;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Value;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class UniqueIndex<A, O extends Entity> extends AbstractHashingAttributeIndex<A, O> {


    protected static final int INDEX_RETRIEVAL_COST = 25;

    private final MVStore store;

    /**
     * Map record structure:
     * <p>
     * <table>
     * <tr>
     * <th>Key</th>
     * <th colspan="2">Value</th>
     * </tr>
     * <tbody>
     * <tr>
     * <td>hash(attribute value)</td>
     * <td>attribute value</td>
     * <td>object value</td>
     * </tr>
     * </tbody>
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

    public static <A, O extends Entity> UniqueIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute) {
        return onAttribute(store, attribute, Hashing.sha1());
    }

    public static <A, O extends Entity> UniqueIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute,
                                                       HashFunction hashFunction) {
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
    public ResultSet<EntityHandle<O>> retrieve(Query<EntityHandle<O>> query, QueryOptions queryOptions) {
        Class<?> queryClass = query.getClass();
        if (queryClass.equals(Equal.class)) {
            final Equal<EntityHandle<O>, A> equal = (Equal<EntityHandle<O>, A>) query;
            byte[] val = map.get(encodeKey(equal.getValue()));
            final EntityHandle<O> obj = val == null ? null : new ResolvedEntityHandle<O>(decodeVal(val).getObject());

            return new ResultSet<EntityHandle<O>>() {
                @Override
                public Iterator<EntityHandle<O>> iterator() {
                    return new UnmodifiableIterator<EntityHandle<O>>() {
                        boolean hasNext = (obj != null);

                        @Override
                        public boolean hasNext() {
                            return this.hasNext;
                        }

                        @Override
                        public EntityHandle<O> next() {
                            this.hasNext = false;
                            return obj;
                        }
                    };
                }

                @Override
                public boolean contains(EntityHandle<O> object) {
                    return (object != null && obj != null && object.equals(obj));
                }

                @Override
                public boolean matches(EntityHandle<O> object) {
                    return query.matches(object, queryOptions);
                }

                @Override
                public Query<EntityHandle<O>> getQuery() {
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

    @Override
    public Index<EntityHandle<O>> getEffectiveIndex() {
        return this;
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
        A attr = attributeDeserializer.deserialize(attrTypeHandler, buffer);
        O obj = objectDeserializer.deserialize(buffer);
        return new Val<>(attr, obj);
    }

    @Override
    public boolean addAll(Collection<EntityHandle<O>> objects, QueryOptions queryOptions) {
        for (EntityHandle<O> object : objects) {
            addObject(queryOptions, object);
        }
        return true;
    }

    public boolean addAll(ObjectStore<EntityHandle<O>> objects, QueryOptions queryOptions) {
        CloseableIterator<EntityHandle<O>> iterator = objects.iterator(queryOptions);
        while (iterator.hasNext()) {
            EntityHandle<O> object = iterator.next();
            addObject(queryOptions, object);
        }
        return true;
    }

    private void addObject(QueryOptions queryOptions, EntityHandle<O> object) {
        for (A value : attribute.getValues(object, queryOptions)) {
            if (value != null) { // Don't index null attribute values
                Entry entry = encodeEntry(object.get(), value);
                if (map.containsKey(entry.getKey()) && !decodeVal(map.get(entry.getKey())).getObject().equals(object)) {
                    throw new com.googlecode.cqengine.index.unique.UniqueIndex.UniqueConstraintViolatedException(
                            "The application has attempted to add a duplicate object to the UniqueIndex on attribute '"
                                    + attribute.getAttributeName() +
                                    "', potentially causing inconsistencies between indexes. " +
                                    "UniqueIndex should not be used with attributes which do not uniquely identify objects. " +
                                    "Problematic attribute value: '" + decodeVal(map.get(entry.getKey()))
                                    .getAttr() + "', " +
                                    "problematic duplicate object: " + object);
                }
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean removeAll(Collection<EntityHandle<O>> objects, QueryOptions queryOptions) {
        for (EntityHandle<O> object : objects) {
            for (A value : attribute.getValues(object, queryOptions)) {
                Entry entry = encodeEntry(object.get(), value);
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
    public void init(ObjectStore<EntityHandle<O>> objectStore, QueryOptions queryOptions) {
        addAll(objectStore, queryOptions);
    }

}
