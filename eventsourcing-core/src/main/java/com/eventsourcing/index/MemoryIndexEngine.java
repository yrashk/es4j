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
import com.eventsourcing.Repository;
import com.eventsourcing.Journal;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.compound.CompoundIndex;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex;
import com.googlecode.cqengine.index.radixreversed.ReversedRadixTreeIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.persistence.Persistence;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.resultset.ResultSet;
import org.osgi.service.component.annotations.Component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Component(property = {"type=MemoryIndexEngine"})
public class MemoryIndexEngine extends CQIndexEngine implements IndexEngine {

    @Override public String getType() {
        return "MemoryIndexEngine";
    }

    @Override
    public void setRepository(Repository repository) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.repository = repository;
    }

    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.journal = journal;
    }

    @Override
    protected List<IndexCapabilities> getIndexMatrix() {
        return Arrays.asList(
                new IndexCapabilities<Attribute>("Hash",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.QZ},
                                                 attr -> HashIndex.onAttribute(compatibleAttribute(attr))),
                new IndexCapabilities<Attribute>("Unique",
                                                 new IndexFeature[]{IndexFeature.UNIQUE, IndexFeature.EQ, IndexFeature.IN},
                                                 attr -> UniqueIndex.onAttribute(compatibleAttribute(attr))),
                new IndexCapabilities<Attribute[]>("Compound",
                                                   new IndexFeature[]{IndexFeature.COMPOUND, IndexFeature.EQ, IndexFeature.IN, IndexFeature.QZ},
                                                   attrs -> {
                                                       Attribute[] attributes = (Attribute[]) Arrays.stream(attrs)
                                                                                                    .map(attr -> compatibleAttribute(attr))
                                                                                                    .toArray();
                                                       return CompoundIndex.onAttributes(attributes);
                                                   }),
                new IndexCapabilities<Attribute>("Navigable",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.QZ, IndexFeature.LT, IndexFeature.GT, IndexFeature.BT},
                                                 attr -> NavigableIndex.onAttribute(compatibleAttribute(attr))),
                new IndexCapabilities<Attribute>("RadixTree",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.SW},
                                                 attr -> RadixTreeIndex.onAttribute(compatibleAttribute(attr))),
                new IndexCapabilities<Attribute>("ReversedRadixTree",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.EW},
                                                 attr -> ReversedRadixTreeIndex.onAttribute(compatibleAttribute(attr))),
                new IndexCapabilities<Attribute>("InvertedRadixTree",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.CI},
                                                 attr -> InvertedRadixTreeIndex.onAttribute(compatibleAttribute(attr))),
                new IndexCapabilities<Attribute>("SuffixTree",
                                                 new IndexFeature[]{IndexFeature.EQ, IndexFeature.IN, IndexFeature.EW, IndexFeature.SC},
                                                 attr -> SuffixTreeIndex.onAttribute(compatibleAttribute(attr)))
        );

    }

    @Override protected <T extends Entity> IndexedCollection<EntityHandle<T>>
            createIndexedCollection(Persistence<EntityHandle<T>, ? extends Comparable> persistence) {
        return new ConcurrentMemoryIndexedCollection<>(persistence);
    }

    private static class ConcurrentMemoryIndexedCollection<O> extends ConcurrentIndexedCollection<O> {
        public ConcurrentMemoryIndexedCollection() {
            super();
        }

        public ConcurrentMemoryIndexedCollection(
                Persistence<O, ? extends Comparable> persistence) {
            super(persistence);
        }

        @Override public ResultSet<O> retrieve(Query<O> query) {
            return super.retrieve(compatibleQuery(query));
        }

        @Override public ResultSet<O> retrieve(Query<O> query, QueryOptions queryOptions) {
            return super.retrieve(compatibleQuery(query), queryOptions);
        }

        private Query<O> compatibleQuery(Query<O> query) {
            if (query instanceof Equal) {
                if (((Equal) query).getAttributeType() == byte[].class) {
                    Equal<O, Object> newQuery = new SpecialEquality<>(query);
                    return newQuery;
                }
            }
            return query;
        }

        private static class SpecialEquality<O> extends Equal<O, Object> {
            public SpecialEquality(Query<O> query) {
                super(compatibleAttribute(((Equal) query).getAttribute()), ((Equal) query).getValue());
            }

            @Override
            protected boolean matchesNonSimpleAttribute(Attribute<O, Object> attribute, O object,
                                                        QueryOptions queryOptions) {
                for (Object attributeValue : attribute.getValues(object, queryOptions)) {
                    if (attributeValue.equals(getValue())) {
                        return true;
                    }
                }
                return false;
            }

            @Override public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Equal)) return false;

                Equal equal = (Equal) o;

                if (!equal.getAttribute().equals(getAttribute())) return false;
                if (!equal.getValue().equals(getValue())) return false;
                return true;
            }
        }
    }

    public static <O extends Entity, A> Attribute<EntityHandle<O>, ?> compatibleAttribute(Attribute<EntityHandle<O>, A>
                                                                                       attribute) {
        if (attribute.getAttributeType() == byte[].class) {
            return new ByteArrayWrappingAttribute<>((Attribute<EntityHandle<O>, byte[]>) attribute);
        } else {
            return attribute;
        }
    }

    private static class ByteArrayWrappingAttribute<O extends Entity>
            extends MultiValueAttribute<EntityHandle<O>, ByteArray> {
        private final Attribute<EntityHandle<O>, byte[]> attribute;

        public ByteArrayWrappingAttribute(Attribute<EntityHandle<O>, byte[]> attribute) {
            this.attribute = attribute;
        }

        @Override public String getAttributeName() {
            return attribute.getAttributeName();
        }

        @Override public Iterable<ByteArray> getValues(EntityHandle<O> object, QueryOptions queryOptions) {
            ArrayList<ByteArray> objects = new ArrayList<>();
            Iterable<byte[]> values = attribute.getValues(object, queryOptions);
            Iterator<byte[]> iterator = values.iterator();
            while (iterator.hasNext()) {
                objects.add(new ByteArray(iterator.next()));
            }
            return objects;
        }

        @Override public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o.equals(attribute)) {
                return true;
            }
            return super.equals(o);
        }
    }

    private static class ByteArray implements Comparable {
        private final byte[] bytes;

        private ByteArray(byte[] bytes) {this.bytes = bytes;}

        @Override public boolean equals(Object obj) {
            if (!(obj instanceof byte[]) && !(obj instanceof ByteArray)) {
                return false;
            }
            if (obj instanceof ByteArray) {
                return ByteBuffer.wrap(bytes).equals(ByteBuffer.wrap(((ByteArray) obj).bytes));
            }
            return ByteBuffer.wrap(bytes).equals(ByteBuffer.wrap((byte[]) obj));
        }

        @Override public int compareTo(Object o) {
            if (!(o instanceof byte[])) {
                return -1;
            }
            return ByteBuffer.wrap(bytes).compareTo(ByteBuffer.wrap((byte[]) o));
        }
    }
}
