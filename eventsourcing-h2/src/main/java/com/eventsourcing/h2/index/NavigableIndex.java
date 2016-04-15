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
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.support.*;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.*;
import com.googlecode.cqengine.resultset.ResultSet;
import lombok.Value;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NavigableIndex<A extends Comparable<A>, O> extends AbstractHashingAttributeIndex<A, O> implements SortedKeyStatisticsIndex<A, O> {

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
     *           <td>attribute value</td>
     *           <td>hash(object value)</td>
     *           <td>hash(attribute value)</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    private final MVMap<byte[], byte[]> map;

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
     * @param hashFunction
     */
    protected NavigableIndex(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
        super(attribute, new HashSet<Class<? extends Query>>() {{
            add(Equal.class);
            add(LessThan.class);
            add(GreaterThan.class);
            add(Between.class);
            add(Has.class);
        }}, hashFunction);
        this.store = store;
        String classname = attribute.getObjectType().getName();
        map = store.openMap("navigable_index_" + classname + "_" + attribute.getAttributeName());
        objHashMap = store.openMap("navigable_index_objhash_" + classname + "_" + attribute.getAttributeName());
        attrHashMap = store.openMap("navigable_index_attrhash_" + classname + "_" + attribute.getAttributeName());
    }

    public static <O, A extends Comparable<A>> NavigableIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute) {
        return onAttribute(store, attribute, Hashing.sha1());
    }

    public static <O, A extends Comparable<A>> NavigableIndex<A, O> onAttribute(MVStore store, Attribute<O, A> attribute, HashFunction hashFunction) {
        return new NavigableIndex<>(store, attribute, hashFunction);
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
            byte[] hash = map.get(cursor.next());
            return attributeDeserializer.deserialize(ByteBuffer.wrap(attrHashMap.get(hash)));
        }
    }

    @Override
    public CloseableIterable<A> getDistinctKeys(QueryOptions queryOptions) {
        return new Iterable(map.cursor(map.firstKey()));
    }

    @Override
    public Integer getCountForKey(A key, QueryOptions queryOptions) {
        byte[] attr = encodeAttribute(key);
        Cursor<byte[], byte[]> cursor = map.cursor(map.ceilingKey(attr));
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
        for (A key : getDistinctKeysDescending(queryOptions)) {
            statistics.add(new KeyStatistics<>(key, getCountForKey(key, queryOptions)));
        }
        Iterator<KeyStatistics<A>> iterator = statistics.iterator();
        return new KeyStatisticsCloseableIterable(iterator);
    }

    @Override
    public CloseableIterable<A> getDistinctKeys(A lowerBound, boolean lowerInclusive, A upperBound, boolean upperInclusive, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public CloseableIterable<A> getDistinctKeysDescending(QueryOptions queryOptions) {
        // FIXME: Grossly inefficient implementation since MVMap does not support descending iterations
        CloseableIterable<A> distinctKeys = getDistinctKeys(queryOptions);
        ArrayDeque<A> coll = StreamSupport.stream(distinctKeys.spliterator(), false).collect(Collectors.toCollection(ArrayDeque::new));
        Iterator<A> it = coll.descendingIterator();
        return () -> new CloseableIterator<A>() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public A next() {
                return it.next();
            }
        };
    }

    @Override
    public CloseableIterable<A> getDistinctKeysDescending(A lowerBound, boolean lowerInclusive, A upperBound, boolean upperInclusive, QueryOptions queryOptions) {
        return null;
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

    @Override
    public CloseableIterable<KeyStatistics<A>> getStatisticsForDistinctKeysDescending(QueryOptions queryOptions) {
        List<KeyStatistics<A>> statistics = new ArrayList<>();
        for (A key : getDistinctKeysDescending(queryOptions)) {
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
    public CloseableIterable<KeyValue<A, O>> getKeysAndValues(A lowerBound, boolean lowerInclusive, A upperBound, boolean upperInclusive, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public CloseableIterable<KeyValue<A, O>> getKeysAndValuesDescending(QueryOptions queryOptions) {
        return null;
    }

    @Override
    public CloseableIterable<KeyValue<A, O>> getKeysAndValuesDescending(A lowerBound, boolean lowerInclusive, A upperBound, boolean upperInclusive, QueryOptions queryOptions) {
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
        return null;
    }

    @Value
    static class Entry {
        private byte[] key;
        private byte[] value;
        private byte[] attrHash;
        private byte[] valueHash;
        private byte[] attr;
        private byte[] comparableAttr;
    }

    private byte[] encodeAttribute(A value) {
        int comparableAttributeSize = attributeSerializer.comparableSize(value);
        ByteBuffer comparableAttribute = ByteBuffer.allocate(comparableAttributeSize);
        attributeSerializer.serializeComparable(value, comparableAttribute);

        return comparableAttribute.array();
    }

    private Entry encodeEntry(O object, A value) {
        int attributeSize = attributeSerializer.size(value);
        ByteBuffer serializedAttribute = ByteBuffer.allocate(attributeSize);
        attributeSerializer.serialize(value, serializedAttribute);

        int comparableAttributeSize = attributeSerializer.comparableSize(value);
        ByteBuffer comparableAttribute = ByteBuffer.allocate(comparableAttributeSize);
        attributeSerializer.serializeComparable(value, comparableAttribute);

        int objectSize = objectSerializer.size(object);
        ByteBuffer serializedObject = ByteBuffer.allocate(objectSize);
        objectSerializer.serialize(object, serializedObject);

        ByteBuffer buffer = ByteBuffer.allocate(comparableAttribute.array().length + hashSize);

        buffer.put(comparableAttribute.array());

        byte[] valueHash = hashFunction.hashBytes(serializedObject.array()).asBytes();
        buffer.put(valueHash);

        byte[] attrHash = hashFunction.hashBytes(serializedAttribute.array()).asBytes();

        return new Entry(buffer.array(), attrHash, attrHash, valueHash, serializedAttribute.array(), comparableAttribute.array());
    }
//
//    public A decodeKey(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        byte[] hash = new byte[hashSize];
//        buffer.get(hash);
//        return attributeDeserializer.deserialize(ByteBuffer.wrap(attrHashMap.get(hash)));
//    }

    @Override
    public boolean addAll(Collection<O> objects, QueryOptions queryOptions) {
        for (O object : objects) {
            for (A value : attribute.getValues(object, queryOptions)) {
                if (value != null) { // Don't index null attribute values
                    Entry entry = encodeEntry(object, value);
                    map.put(entry.getKey(), entry.getValue());
                    attrHashMap.put(entry.getAttrHash(), entry.getAttr());
                    objHashMap.putIfAbsent(entry.getValueHash(), entry.getValue());
                }
            }
        }
        return true;

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
        addAll(collection, queryOptions);
    }
}
