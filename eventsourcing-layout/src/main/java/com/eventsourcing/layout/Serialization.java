/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import java.util.HashMap;

public abstract class Serialization {
    protected HashMap<TypeHandler, Serializer<?, ? extends TypeHandler>> serializers = new HashMap<>();
    protected HashMap<TypeHandler, Deserializer<?, ? extends TypeHandler>> deserializers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T, H extends TypeHandler> Serializer<T, H> getSerializer(H typeHandler) {
        Serializer<T, H> s = (Serializer<T, H>) serializers.get(typeHandler);
        if (s == null) {
            throw new RuntimeException("No " + getClass().getName() + " serializer for " + typeHandler.getClass() + " has " +
                                               "been found");
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public <T, H extends TypeHandler> Deserializer<T, H> getDeserializer(H typeHandler) {
        Deserializer<T, H> ds = (Deserializer<T, H>) deserializers.get(typeHandler);
        if (ds == null) {
            throw new RuntimeException("No " + getClass().getName() + " deserializer for " + typeHandler.getClass() + " has " +
                                               "been found");
        }
        return ds;
    }

    public <H extends TypeHandler> void addSerializer(H typeHandler, Serializer<?, H> serializer) {
        serializers.put(typeHandler, serializer);
        if (!serializers.containsKey(typeHandler)) {
            throw new RuntimeException("Can't store type handler " + typeHandler);
        }
    }

    public <H extends TypeHandler> void addDeserializer(H typeHandler, Deserializer<?, H> deserializer) {
        deserializers.put(typeHandler, deserializer);
        if(!deserializers.containsKey(typeHandler)) {
            throw new RuntimeException("Can't store type handler " + typeHandler);
        }
    }

    public abstract <T> ObjectSerializer<T> getSerializer(Class<?> klass);
    public <T> ObjectDeserializer<T> getDeserializer(Class<?> klass) {
        return getDeserializer(klass, false);
    }
    public abstract <T> ObjectDeserializer<T> getDeserializer(Class<?> klass, boolean allowReadonly);
}
