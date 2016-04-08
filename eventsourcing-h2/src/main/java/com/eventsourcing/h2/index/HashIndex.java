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
package com.eventsourcing.h2.index;

import com.eventsourcing.index.AbstractHashingAttributeIndex;
import com.google.common.collect.Iterators;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.support.*;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.query.simple.Has;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Value;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class HashIndex<A, O> extends AbstractHashingAttributeIndex<A, O> implements KeyStatisticsAttributeIndex<A, O> {

    protected static final int INDEX_RETRIEVAL_COST = 30;

    private final MVStore store;

    /**
     * Map record structure:
     *
     * <table>
     *     <tr>
     *         <th colspan="2">Key</th>
     *         <th>Value</th>
     *     </tr>
     *     <tbody>
     *         <tr>
     *           <td>hash(attribute value)</td>
     *           <td>hash(object value)</td>
     *           <td>true</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    private final MVMap<byte[], Boolean> map;
    /**
     * Map record structure:
     *
     * <table>
     *     <tr>
     *         <th>Key</th>
     *         <th>Value</th>
     *     </tr>
     *     <tbody>
     *         <tr>
     *           <td>hash(attribute value)</td>
     *           <td>attribute value</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    private final MVMap<byte[], byte[]> attrHashMap;
    /**
     * Map record structure:
     *
     * <table>
     *     <tr>
     *         <th>Key</th>
     *         <th>Value</th>
     *     </tr>
     *     <tbody>
     *         <tr>
     *           <td>hash(object value)</td>
     *           <td>object value</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    private final MVMap<byte[], byte[]> objHashMap;

    /**
     * Protected constructor, called by subclasses.
     *
     * @param attribute        The attribute on which the index will be built
     */
    protected HashIndex(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
        super(attribute, new HashSet<Class<? extends Query>>() {{
            add(Equal.class);
            add(Has.class);
        }}, hashFunction);
        this.store = store;
        String classname = attribute.getObjectType().getName();
        map = store.openMap("hash_index_" + classname + "_" + attribute.getAttributeName());
        attrHashMap = store.openMap("hash_index_attrhash_" + classname + "_" + attribute.getAttributeName());
        objHashMap = store.openMap("hash_index_objhash_" + classname + "_" + attribute.getAttributeName());
    }

    public static <A, O> HashIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute) {
        return onAttribute(store, attribute, Hashing.sha1());
    }
    public static <A, O> HashIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
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
            public void close() throws IOException {

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
        public void close() throws IOException {
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
    public CloseableIterable<KeyValue<A, O>> getKeysAndValues(QueryOptions queryOptions) {
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
    public ResultSet<O> retrieve(Query<O> query, QueryOptions queryOptions) {
        Class<?> queryClass = query.getClass();
        if (queryClass.equals(Equal.class)) {
            final Equal<O, A> equal = (Equal<O, A>) query;
            byte[] attr = encodeAttribute(equal.getValue());
            byte[] from = map.ceilingKey(attr);

            return new ResultSet<O>() {
                @Override
                public Iterator<O> iterator() {
                    boolean empty = Bytes.indexOf(from, attr) != 0;
                    Cursor<byte[], Boolean> cursor = map.cursor(from);
                    if (empty) {
                        return Collections.<O>emptyList().iterator();
                    }
                    return new CursorIterator(cursor, attr);
                }

                @Override
                public boolean contains(O object) {
                    Entry entry = encodeEntry(object, equal.getValue());
                    return objHashMap.containsKey(entry.getValueHash());
                }

                @Override
                public boolean matches(O object) {
                    return equal.matches(object, queryOptions);
                }

                @Override
                public Query<O> getQuery() {
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
        }  else if (queryClass.equals(Has.class)) {
            final Has<O, A> has = (Has<O, A>) query;
            byte[] from = map.firstKey();

            return new ResultSet<O>() {

                @Override
                public Iterator<O> iterator() {
                    Cursor<byte[], Boolean> cursor = map.cursor(from);
                    return new CursorIterator(cursor, new byte[]{});
                }

                @Override
                public boolean contains(O object) {
                    ByteBuffer buffer = ByteBuffer.allocate(objectSerializer.size(object));
                    objectSerializer.serialize(object, buffer);
                    return objHashMap.containsKey(hashFunction.hashBytes(buffer.array()).asBytes());
                }

                @Override
                public boolean matches(O object) {
                    return has.matches(object, queryOptions);
                }

                @Override
                public Query<O> getQuery() {
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
    public boolean addAll(Collection<O> objects, QueryOptions queryOptions) {
        for (O object : objects) {
            for (A value : attribute.getValues(object, queryOptions)) {
                if (value != null) { // Don't index null attribute values
                    ByteBuffer buffer = ByteBuffer.allocate(objectSerializer.size(object));
                    objectSerializer.serialize(object, buffer);
                    Entry entry = encodeEntry(object, value);
                    map.put(entry.getKey(), true);
                    attrHashMap.putIfAbsent(entry.getAttrHash(), entry.getAttr());
                    objHashMap.putIfAbsent(entry.getValueHash(), entry.getValue());
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

    private class CursorIterator implements Iterator<O> {

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
        public O next() {
            ByteBuffer buffer = ByteBuffer.wrap(next);
            buffer.position(hashSize); // skip attribute hash
            byte[] hash = new byte[hashSize];
            buffer.get(hash);
            return objectDeserializer.deserialize(ByteBuffer.wrap(objHashMap.get(hash)));
        }
    }
}
