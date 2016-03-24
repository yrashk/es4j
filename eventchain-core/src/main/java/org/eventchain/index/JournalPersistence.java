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
package org.eventchain.index;

import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.persistence.Persistence;
import com.googlecode.cqengine.query.option.QueryOptions;
import org.eventchain.Entity;
import org.eventchain.EntityHandle;
import org.eventchain.Journal;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

public abstract class JournalPersistence<T extends Entity> implements Persistence<EntityHandle<T>, UUID> {

    protected final Journal journal;
    protected final Class<T> klass;

    public JournalPersistence(Journal journal, Class<T> klass) {
        this.journal = journal;
        this.klass = klass;
    }

    @Override
    public SimpleAttribute<EntityHandle<T>, UUID> getPrimaryKeyAttribute() {
        return new SimpleAttribute<EntityHandle<T>, UUID>() {
            @Override
            public UUID getValue(EntityHandle<T> object, QueryOptions queryOptions) {
                return object.uuid();
            }
        };
    }

    @Override
    public long getBytesUsed() {
        return 0;
    }

    @Override
    public void compact() {

    }

    @Override
    public void expand(long numBytes) {
    }

    @Override
    public Connection getConnection(Index<?> index) {
        return null;
    }

    @Override
    public boolean isApplyUpdateForIndexEnabled(Index<?> index) {
        return false;
    }

    @Override
    public abstract Set<EntityHandle<T>> create();

    @Override
    public boolean supportsIndex(Index<?> index) {
        return true;
    }

    static abstract class JournalSet<T extends Entity> implements Set<EntityHandle<T>> {

        protected final Journal journal;
        protected final Class<T> klass;

        public JournalSet(Journal journal, Class<T> klass) {
            this.journal = journal;
            this.klass = klass;
        }

        @Override
        public int size() {
            return (int) journal.size(klass);
        }

        @Override
        public boolean isEmpty() {
            return journal.isEmpty(klass);
        }

        @Override
        public boolean contains(Object o) {
            return journal.get(((EntityHandle<T>)o).uuid()).isPresent();
        }

        @Override
        public abstract Iterator<EntityHandle<T>> iterator();

        @Override
        public Object[] toArray() {
                return StreamSupport.stream(spliterator(), false).toArray();
        }

        @Override @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            return (T[]) toArray();
        }

        @Override
        public boolean add(EntityHandle<T> tEntityHandle) {
            return true; // this is taken care of with journalling
        }

        @Override
        public boolean remove(Object o) {
            return false; // immutable set
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return c.stream().allMatch(this::contains);
        }

        @Override
        public boolean addAll(Collection<? extends EntityHandle<T>> c) {
            return true; // this is taken care of with journalling
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false; // immutable set
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false; // immutable set
        }

        @Override
        public void clear() {
            // immutable set
        }
    }

}
