/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.primitives.Bytes;
import lombok.Getter;

import java.util.List;

public class MapTypeHandler implements TypeHandler {

    @Getter
    private final TypeHandler wrappedKeyHandler;
    @Getter
    private final TypeHandler wrappedValueHandler;

    public MapTypeHandler() {
        wrappedKeyHandler = null;
        wrappedValueHandler = null;
    }

    public MapTypeHandler(List<ResolvedType> typeParameters) throws TypeHandlerException {
        if (typeParameters.size() != 2) {
            throw new IllegalArgumentException("Map type parameters should be specified");
        }
        ResolvedType resolvedKeyType = new TypeResolver().resolve(typeParameters.get(0));
        wrappedKeyHandler = TypeHandler.lookup(resolvedKeyType);
        ResolvedType resolvedValueType = new TypeResolver().resolve(typeParameters.get(1));
        wrappedValueHandler = TypeHandler.lookup(resolvedValueType);
    }

    @Override
    public byte[] getFingerprint() {
        return Bytes.concat("Map[".getBytes(), wrappedKeyHandler.getFingerprint(), "]".getBytes(),"[".getBytes(),
                            wrappedValueHandler.getFingerprint(), "]".getBytes());
    }

    @Override public int hashCode() {
        return "Map".hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof MapTypeHandler && obj.hashCode() == hashCode();
    }
}
