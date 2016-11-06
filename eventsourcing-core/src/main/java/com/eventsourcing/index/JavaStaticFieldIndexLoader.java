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
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.googlecode.cqengine.index.Index;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

@Component
@Slf4j
public class JavaStaticFieldIndexLoader implements IndexLoader {

    @SneakyThrows
    @Override public Iterable<Index> load(IndexEngine engine, Class entityClass) {
        List<com.googlecode.cqengine.index.Index> indices = new ArrayList<>();
        Class<?>[] classes = new Class[]{entityClass};
        if (entityClass.isAnnotationPresent(Indices.class)) {
            classes = ((Indices)entityClass.getAnnotation(Indices.class)).value();
        }
        for (Class<?> klass : classes) {
            for (Field field : klass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &&
                        EntityIndex.class.isAssignableFrom(field.getType())) {

                    com.eventsourcing.index.Index annotation = field
                            .getAnnotation(com.eventsourcing.index.Index.class);
                    IndexEngine.IndexFeature[] features = annotation == null ? new IndexEngine.IndexFeature[]{EQ} : annotation.value();
                    EntityIndex index = ((EntityIndex) field.get(null));
                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    Class<Entity> objectType = (Class<Entity>) type.getActualTypeArguments()[0];
                    Type attributeType = type.getActualTypeArguments()[1];
                    Class<EntityHandle<Entity>> entityType = (Class<EntityHandle<Entity>>) field.getType()
                                                                                                .getInterfaces()[0];
                    Attribute attribute;

                    if (SimpleIndex.class.isAssignableFrom(field.getType())) {
                        attribute = new EntitySimpleAttribute(objectType, entityType, attributeType, field, index);
                        if (index instanceof IndexWithAttribute) {
                            ((IndexWithAttribute) index).setAttribute(attribute);
                        } else {
                            log.warn("Non-final index definitions are deprecated, use SimpleIndex.as() instead");
                            WrappedSimpleIndex wrappedIndex = new WrappedSimpleIndex<>((BiFunction<Entity, QueryOptions, Object>) ((SimpleIndex) index)::getValue);
                            wrappedIndex.setAttribute(attribute);
                            field.set(null, wrappedIndex);
                        }
                    } else {
                        attribute = new MultiValueEntityAttribute(objectType, entityType, attributeType, field, index);
                        if (index instanceof IndexWithAttribute) {
                            ((IndexWithAttribute) index).setAttribute(attribute);
                        } else {
                            log.warn("Non-final index definitions are deprecated, use MultiValueIndex.as() instead");
                            WrappedMultiValueIndex wrappedIndex = new WrappedMultiValueIndex(((BiFunction<Entity,
                                    QueryOptions, Object>) ((MultiValueIndex) index)::getValues));
                            wrappedIndex.setAttribute(attribute);
                            field.set(null, wrappedIndex);
                        }
                    }
                    indices.add(engine.getIndexOnAttribute(attribute, features));
                }
            }
        }
        return indices;
    }



    class EntitySimpleAttribute extends SimpleAttribute<Entity, Object> {
        @Getter
        private final Type attributeReflectedType;
        private final EntityIndex index;

        public EntitySimpleAttribute(Class<Entity> objectType, Class<EntityHandle<Entity>> entityType,
                                     Type attributeType,
                                     Field field, EntityIndex index) {
            super(objectType, entityType,
                  (Class<Object>) (attributeType instanceof ParameterizedType ? ((ParameterizedType) attributeType)
                          .getRawType(): attributeType),
                  field.getName());
            this.attributeReflectedType = attributeType;
            this.index = index;
        }

        @Override public Object getValue(Entity object, QueryOptions queryOptions) {
            return ((SimpleIndex) index).getValue(object, queryOptions);
        }
    }

    class MultiValueEntityAttribute extends MultiValueAttribute<Entity, Object> {
        @Getter
        private final Type attributeReflectedType;
        private final EntityIndex index;

        public MultiValueEntityAttribute(Class<Entity> objectType, Class<EntityHandle<Entity>> entityType,
                                         Type attributeType,
                                         Field field, EntityIndex index) {
            super(objectType, entityType, (Class<Object>) (attributeType instanceof ParameterizedType ? ((ParameterizedType) attributeType)
                    .getRawType(): attributeType), field.getName());
            this.attributeReflectedType = attributeType;
            this.index = index;
        }

        @Override public Iterable<Object> getValues(Entity object, QueryOptions queryOptions) {
            return index.getValues(object, queryOptions);
        }
    }
}
