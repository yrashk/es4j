/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.fasterxml.classmate.ResolvedType;
import lombok.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Property represents POJO's property name, type and getter/setter.
 *
 * Normally not created manually but retrieved from {@link Layout}
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

    public String toString() {
        return name + ": " + type.getBriefDescription();
    }
}
