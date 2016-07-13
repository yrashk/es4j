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
import com.eventsourcing.Journal;
import com.eventsourcing.Repository;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Service;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

public interface IndexEngine extends Service {

    String getType();

    /**
     * Sets journal to be used in this repository
     * <p>
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

    @SuppressWarnings("unchecked") <O extends Entity, A> Index<O> getIndexOnAttributes(Attribute<O, A>[] attributes,
                                                                                               IndexFeature... features)
            throws IndexNotSupported;

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

    @Value class IndexCapabilities<T> {
        private String name;
        private IndexFeature[] features;
        private Function<T, Index> index;
    }

    <O extends Entity, A> Index<O> getIndexOnAttribute(Attribute<O, A> attribute, IndexFeature... features) throws
                                                                                                IndexNotSupported;

    @AllArgsConstructor class IndexNotSupported extends Exception {
        @Getter
        private Attribute[] attribute;
        @Getter
        private IndexFeature[] features;
        @Getter
        private IndexEngine indexEngine;

        @Override
        public String getMessage() {
            return "Index " + Joiner.on(", ").join(features) + " on " + Joiner.on(", ")
                                                                              .join(attribute) + " is not supported by " + indexEngine;
        }
    }

    <T extends Entity> IndexedCollection<EntityHandle<T>> getIndexedCollection(Class<T> klass);

    /**
     * Returns all declared ({@link com.eventsourcing.index.Index}) indices for a class
     *
     * @param entityClass
     * @return
     * @throws IndexNotSupported
     * @throws IllegalAccessException
     */
    default Iterable<Index> getIndices(Class<?> entityClass) throws IndexNotSupported, IllegalAccessException {
        List<Index> indices = new ArrayList<>();
        Class<?>[] classes = new Class[]{entityClass};
        if (entityClass.isAnnotationPresent(Indices.class)) {
            classes = entityClass.getAnnotation(Indices.class).value();
        }
        for (Class<?> klass : classes) {
            for (Field field : klass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &&
                        EntityIndex.class.isAssignableFrom(field.getType())) {
                    if (Modifier.isFinal(field.getModifiers())) {
                        throw new IllegalArgumentException("Index attribute " + field + " can't be declared final");
                    }

                    com.eventsourcing.index.Index annotation = field
                            .getAnnotation(com.eventsourcing.index.Index.class);
                    IndexFeature[] features = annotation == null ? new IndexFeature[]{EQ} : annotation.value();
                    EntityIndex index = ((EntityIndex) field.get(null));
                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    Class<Entity> objectType = (Class<Entity>) type.getActualTypeArguments()[0];
                    Class<Object> attributeType = (Class<Object>) type.getActualTypeArguments()[1];
                    Class<EntityHandle<Entity>> entityType = (Class<EntityHandle<Entity>>) field.getType()
                                                                                                .getInterfaces()[0];
                    Attribute attribute;

                    if (SimpleIndex.class.isAssignableFrom(field.getType())) {
                        attribute = new EntitySimpleAttribute(objectType, entityType, attributeType, field, index);
                        field.set(null, new SimpleIndex() {
                            @Override public Attribute getAttribute() {
                                return attribute;
                            }

                            @Override public Object getValue(Entity object, QueryOptions queryOptions) {
                                return ((SimpleIndex) index).getValue(object, queryOptions);
                            }
                        });
                    } else {
                        attribute = new MultiValueEntityAttribute(objectType, entityType, attributeType, field, index);
                        field.set(null, new MultiValueIndex() {
                            @Override public Attribute getAttribute() {
                                return attribute;
                            }

                            @Override public Iterable getValues(Entity object, QueryOptions queryOptions) {
                                return index.getValues(object, queryOptions);
                            }
                        });
                    }
                    indices.add(this.getIndexOnAttribute(attribute, features));
                }
            }
        }
        return indices;
    }


    class EntitySimpleAttribute extends SimpleAttribute<Entity, Object> {
        private final EntityIndex index;

        public EntitySimpleAttribute(Class<Entity> objectType, Class<EntityHandle<Entity>> entityType,
                                     Class<Object> attributeType,
                                     Field field, EntityIndex index) {
            super(objectType, entityType, attributeType, field.getName());
            this.index = index;
        }

        @Override public Object getValue(Entity object, QueryOptions queryOptions) {
            return ((SimpleIndex) index).getValue(object, queryOptions);
        }
    }

    class MultiValueEntityAttribute extends MultiValueAttribute {
        private final EntityIndex index;

        public MultiValueEntityAttribute(Class<Entity> objectType, Class<EntityHandle<Entity>> entityType,
                                         Class<Object> attributeType,
                                         Field field, EntityIndex index) {
            super(objectType, entityType, attributeType, field.getName());
            this.index = index;
        }

        @Override public Iterable<Object> getValues(Entity object, QueryOptions queryOptions) {
            return index.getValues(object, queryOptions);
        }
    }
}
