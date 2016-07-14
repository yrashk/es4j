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
import com.eventsourcing.ResolvedEntityHandle;
import com.eventsourcing.index.AbstractHashingAttributeIndex;
import com.eventsourcing.index.Attribute;
import com.google.common.collect.Iterators;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.support.*;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.query.simple.Has;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Value;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.nio.ByteBuffer;
import java.util.*;

public class HashIndex<A, O extends Entity> extends AbstractHashingAttributeIndex<A, O> implements
        KeyStatisticsAttributeIndex<A, EntityHandle<O>> {

    protected static final int INDEX_RETRIEVAL_COST = 30;

    private final MVStore store;

    /**
     * Map record structure:
     * <p>
     * <table>
     * <tr>
     * <th colspan="2">Key</th>
     * <th>Value</th>
     * </tr>
     * <tbody>
     * <tr>
     * <td>hash(attribute value)</td>
     * <td>hash(object value)</td>
     * <td>true</td>
     * </tr>
     * </tbody>
     * </table>
     */
    private final MVMap<byte[], Boolean> map;
    /**
     * Map record structure:
     * <p>
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tbody>
     * <tr>
     * <td>hash(attribute value)</td>
     * <td>attribute value</td>
     * </tr>
     * </tbody>
     * </table>
     */
    private final MVMap<byte[], byte[]> attrHashMap;
    /**
     * Map record structure:
     * <p>
     * <table>
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tbody>
     * <tr>
     * <td>hash(object value)</td>
     * <td>object value</td>
     * </tr>
     * </tbody>
     * </table>
     */
    private final MVMap<byte[], byte[]> objHashMap;

    /**
     * Protected constructor, called by subclasses.
     *
     * @param attribute The attribute on which the index will be built
     */
    protected HashIndex(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
        super(attribute, new HashSet<Class<? extends Query>>() {{
            add(Equal.class);
            add(Has.class);
        }}, hashFunction);
        this.store = store;
        String classname = attribute.getEffectiveObjectType().getName();
        map = store.openMap("hash_index_" + classname + "_" + attribute.getAttributeName());
        attrHashMap = store.openMap("hash_index_attrhash_" + classname + "_" + attribute.getAttributeName());
        objHashMap = store.openMap("hash_index_objhash_" + classname + "_" + attribute.getAttributeName());
    }

    public static <A, O extends Entity> HashIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute) {
        return onAttribute(store, attribute, Hashing.sha1());
    }

    public static <A, O extends Entity> HashIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute,
                                                     HashFunction hashFunction) {
        return new HashIndex<>(store, attribute, hashFunction);
    }

    private class KeyStatisticsCloseableIterable implements CloseableIterable<KeyStatistics<A>> {
        private final Iterator<KeyStatistics<A>> iterator;

        public KeyStatisticsCloseableIterable(Iterator<KeyStatistics<A>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public CloseableIterator<KeyStatistics<A>> iterator() {
            return new KeyStatisticsCloseableIterator(iterator);
        }

        private class KeyStatisticsCloseableIterator implements CloseableIterator<KeyStatistics<A>> {
            private final Iterator<KeyStatistics<A>> iterator;

            public KeyStatisticsCloseableIterator(Iterator<KeyStatistics<A>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public KeyStatistics<A> next() {
                return iterator.next();
            }
        }
    }

    class CursorAttributeIterator implements CloseableIterator<A> {

        private final Cursor<byte[], byte[]> cursor;

        public CursorAttributeIterator(Cursor<byte[], byte[]> cursor) {
            this.cursor = cursor;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean hasNext() {
            return cursor.hasNext();
        }

        @Override
        public A next() {
            return attributeDeserializer.deserialize(ByteBuffer.wrap(attrHashMap.get(cursor.next())));
        }
    }

    class Iterable implements CloseableIterable<A> {

        private final Cursor<byte[], byte[]> cursor;

        public Iterable(Cursor<byte[], byte[]> cursor) {
            this.cursor = cursor;
        }

        @Override
        public CloseableIterator<A> iterator() {
            return new CursorAttributeIterator(cursor);
        }
    }

    @Value
    static class Entry {
        private byte[] key;
        private byte[] value;
        private byte[] valueHash;
        private byte[] attr;
        private byte[] attrHash;
    }

    private byte[] encodeAttribute(A value) {
        int size = attributeSerializer.size(value);
        ByteBuffer serializedAttribute = ByteBuffer.allocate(size);
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

        ByteBuffer buffer = ByteBuffer.allocate(hashSize * 2);

        byte[] attrHash = hashFunction.hashBytes(serializedAttribute.array()).asBytes();
        buffer.put(attrHash);

        byte[] valueHash = hashFunction.hashBytes(serializedObject.array()).asBytes();
        buffer.put(valueHash);

        return new Entry(buffer.array(), serializedObject.array(), valueHash, serializedAttribute.array(), attrHash);
    }

    public A decodeKey(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] hash = new byte[hashSize];
        buffer.get(hash);
        return attributeDeserializer.deserialize(ByteBuffer.wrap(attrHashMap.get(hash)));
    }

    @Override
    public CloseableIterable<A> getDistinctKeys(QueryOptions queryOptions) {
        return new Iterable(attrHashMap.cursor(attrHashMap.firstKey()));
    }

    @Override
    public Integer getCountForKey(A key, QueryOptions queryOptions) {
        byte[] attr = encodeAttribute(key);
        Cursor<byte[], Boolean> cursor = map.cursor(map.ceilingKey(attr));
        int i = 0;

        while (cursor.hasNext() && Bytes.indexOf(cursor.next(), attr) == 0) {
            i++;
        }

        return i;
    }

    @Override
    public Integer getCountOfDistinctKeys(QueryOptions queryOptions) {
        return attrHashMap.size();
    }

    @Override
    public CloseableIterable<KeyStatistics<A>> getStatisticsForDistinctKeys(QueryOptions queryOptions) {
        List<KeyStatistics<A>> statistics = new ArrayList<>();
        for (A key : getDistinctKeys(queryOptions)) {
            statistics.add(new KeyStatistics<>(key, getCountForKey(key, queryOptions)));
        }
        Iterator<KeyStatistics<A>> iterator = statistics.iterator();
        return new KeyStatisticsCloseableIterable(iterator);
    }

    @Override
    public CloseableIterable<KeyValue<A, EntityHandle<O>>> getKeysAndValues(QueryOptions queryOptions) {
        return null;
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
            byte[] attr = encodeAttribute(equal.getValue());
            byte[] from = map.ceilingKey(attr);

            return new ResultSet<EntityHandle<O>>() {
                @Override
                public Iterator<EntityHandle<O>> iterator() {
                    boolean empty = Bytes.indexOf(from, attr) != 0;
                    Cursor<byte[], Boolean> cursor = map.cursor(from);
                    if (empty) {
                        return Collections.<EntityHandle<O>>emptyList().iterator();
                    }
                    return new CursorIterator(cursor, attr);
                }

                @Override
                public boolean contains(EntityHandle<O> object) {
                    Entry entry = encodeEntry(object.get(), equal.getValue());
                    return objHashMap.containsKey(entry.getValueHash());
                }

                @Override
                public boolean matches(EntityHandle<O> object) {
                    return equal.matches(object, queryOptions);
                }

                @Override
                public Query<EntityHandle<O>> getQuery() {
                    return equal;
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
                    return Iterators.size(iterator());
                }

                @Override
                public int size() {
                    return Iterators.size(iterator());
                }

                @Override
                public void close() {

                }
            };
        } else if (queryClass.equals(Has.class)) {
            final Has<EntityHandle<O>, A> has = (Has<EntityHandle<O>, A>) query;
            byte[] from = map.firstKey();

            return new ResultSet<EntityHandle<O>>() {

                @Override
                public Iterator<EntityHandle<O>> iterator() {
                    Cursor<byte[], Boolean> cursor = map.cursor(from);
                    return new CursorIterator(cursor, new byte[]{});
                }

                @Override
                public boolean contains(EntityHandle<O> object) {
                    ByteBuffer buffer = ByteBuffer.allocate(objectSerializer.size(object.get()));
                    objectSerializer.serialize(object.get(), buffer);
                    return objHashMap.containsKey(hashFunction.hashBytes(buffer.array()).asBytes());
                }

                @Override
                public boolean matches(EntityHandle<O> object) {
                    return has.matches(object, queryOptions);
                }

                @Override
                public Query<EntityHandle<O>> getQuery() {
                    return has;
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
                    return Iterators.size(iterator());
                }

                @Override
                public int size() {
                    return Iterators.size(iterator());
                }

                @Override
                public void close() {

                }
            };
        } else {
            throw new IllegalArgumentException("Unsupported query: " + query);
        }
    }

    @Override
    public Index<EntityHandle<O>> getEffectiveIndex() {
        return this;
    }

    @Override
    public boolean addAll(ObjectSet<EntityHandle<O>> objects, QueryOptions queryOptions) {
        try (CloseableIterator<EntityHandle<O>> iterator = objects.iterator()) {
            while (iterator.hasNext()) {
                addObject(queryOptions, iterator.next());
            }
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
                map.put(entry.getKey(), true);
                attrHashMap.putIfAbsent(entry.getAttrHash(), entry.getAttr());
                objHashMap.putIfAbsent(entry.getValueHash(), entry.getValue());
            }
        }
    }

    @Override
    public boolean removeAll(ObjectSet<EntityHandle<O>> objects, QueryOptions queryOptions) {
        try (CloseableIterator<EntityHandle<O>> iterator = objects.iterator()) {
            while (iterator.hasNext()) {
                EntityHandle<O> object = iterator.next();
                for (A value : attribute.getValues(object, queryOptions)) {
                    Entry entry = encodeEntry(object.get(), value);
                    map.remove(entry.getKey());
                }
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


    private class CursorIterator implements Iterator<EntityHandle<O>> {

        private final Cursor<byte[], Boolean> cursor;
        private final byte[] attr;
        private byte[] next;

        public CursorIterator(Cursor<byte[], Boolean> cursor, byte[] attr) {
            this.cursor = cursor;
            this.attr = attr;
        }

        @Override
        public boolean hasNext() {
            if (cursor.hasNext()) {
                next = cursor.next();
                return Bytes.indexOf(next, attr) == 0;
            } else {
                return false;
            }
        }

        @Override
        public EntityHandle<O> next() {
            ByteBuffer buffer = ByteBuffer.wrap(next);
            buffer.position(hashSize); // skip attribute hash
            byte[] hash = new byte[hashSize];
            buffer.get(hash);
            return new ResolvedEntityHandle<>(objectDeserializer.deserialize(ByteBuffer.wrap(objHashMap.get(hash))));
        }
    }
}
