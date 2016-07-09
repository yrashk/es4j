/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.fasterxml.classmate.ResolvedType;
import lombok.*;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Property represents POJO's property name, type and getter/setter.
 * <p>
 * Normally not created manually but retrieved from {@link Layout}
 *
 * @param <T>
 */
@LayoutName("rfc.eventsourcing.com/spec:7/LDL/#Property")
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:7/LDL/", revision = "Jun 18, 2016")
public class Property<T> {
    @Getter @NonNull
    private String name;
    @Setter
    private byte[] fingerprint;
    @Getter @NonNull
    private ResolvedType type;
    @Getter @NonNull
    private TypeHandler typeHandler;
    @Getter @NonNull
    private Function<T, Object> getter;

    /**
     * Gets property value from the object
     *
     * @param object
     * @return property value
     */
    public <Y> Y get(T object) {
        @SuppressWarnings("unchecked")
        Y v = (Y) getter.apply(object);
        return v;
    }

    public String toString() {
        return name + ": " + type.getBriefDescription();
    }

    public byte[] getFingerprint() {
        if (fingerprint == null && typeHandler != null) {
            return typeHandler.getFingerprint();
        }
        if (fingerprint != null) {
            return fingerprint;
        }
        throw new RuntimeException("fingerprint or typeHandler expected to be non-null");
    }


    public Property(String name, byte[] fingerprint) {
        this.name = name;
        this.fingerprint = fingerprint;
    }


    Property(String name, ResolvedType type, TypeHandler typeHandler, Function<T, Object> getter) {
        this.name = name;
        this.type = type;
        this.typeHandler = typeHandler;
        this.getter = getter;
    }

    @Override public int hashCode() {
        return 31 * getName().hashCode() + 31 * type.hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof Property &&
                ((Property) obj).getName().contentEquals(getName()) &&
                ((Property) obj).getType().equals(getType());
    }
}
