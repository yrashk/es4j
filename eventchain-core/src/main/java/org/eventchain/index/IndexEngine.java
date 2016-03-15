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

import com.google.common.util.concurrent.Service;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import lombok.Value;
import org.eventchain.Entity;
import org.eventchain.EntityHandle;
import org.eventchain.Journal;
import org.eventchain.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IndexEngine extends Service {

    /**
     * Sets journal to be used in this repository
     *
     * Should be done before invoking {@link #startAsync()}
     *
     * @param journal
     * @throws IllegalStateException if called after the service is started
     */
    void setJournal(Journal journal) throws IllegalStateException;

    /**
     * Set repository. Should be done before invoking {@link #startAsync()}
     *
     * @param repository
     * @throws IllegalStateException if called after the service is started
     */
    void setRepository(Repository repository) throws IllegalStateException;

    enum IndexFeature {
        UNIQUE, COMPOUND,

        EQ,
        IN,
        LT,
        GT,
        BT,
        SW,
        EW,
        SC,
        CI,
        RX,
        HS,
        AQ,
        QZ
    }

    @Value
    class IndexCapabilities {
        private String name;
        private IndexFeature[] features;
        private Function<Attribute, Index> index;
    }

    <A, O> Index<A> getIndexOnAttribute(Attribute<A, O> attribute, IndexFeature...features) throws IndexNotSupported;
    class IndexNotSupported extends Exception {}

    <T extends Entity> IndexedCollection<EntityHandle<T>> getIndexedCollection(Class<T> klass);

    /**
     * Returns all declared ({@link org.eventchain.annotations.Index} indices for a class
     * @param klass
     * @return
     * @throws IndexNotSupported
     * @throws IllegalAccessException
     */
    default Iterable<Index> getIndices(Class<?> klass) throws IndexNotSupported, IllegalAccessException {
        List<Index> indices = new ArrayList<>();
        for (Field field : klass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) &&
                Modifier.isPublic(field.getModifiers())) {
                org.eventchain.annotations.Index annotation = field.getAnnotation(org.eventchain.annotations.Index.class);
                if (annotation != null) {
                    Attribute attr = (Attribute) field.get(null);
                    indices.add(this.getIndexOnAttribute(attr, annotation.value()));
                }
            }
        }
        return indices;
    }

}
