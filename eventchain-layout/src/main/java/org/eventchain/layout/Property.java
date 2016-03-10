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
package org.eventchain.layout;

import com.fasterxml.classmate.ResolvedType;
import lombok.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Property represents POJO's property name, type and getter/setter.
 *
 * Normally not created manually but retrieved from {@link Layout<T>}
 * @param <T>
 */
@Value
public class Property<T> {
    private String name;
    private ResolvedType type;
    private TypeHandler<T> typeHandler;
    private BiConsumer<T, Object> setter;
    private Function<T, Object> getter;

    /**
     * Gets property value from the object
     * @param object
     * @return property value
     */
    public <Y> Y get(T object) {
        return (Y) getter.apply(object);
    }

    /**
     * Sets property value to the object
     * @param object
     * @param value
     */
    public void set(T object, Object value) {
        setter.accept(object, value);
    }

}
