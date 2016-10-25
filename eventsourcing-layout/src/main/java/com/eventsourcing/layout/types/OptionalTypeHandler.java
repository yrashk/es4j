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

public class OptionalTypeHandler implements TypeHandler {
    @Getter
    private final TypeHandler wrappedHandler;

    public OptionalTypeHandler() {
        wrappedHandler = null;
    }

    public OptionalTypeHandler(List<ResolvedType> typeParameters) throws TypeHandlerException {
        if (typeParameters.size() != 1) {
            throw new IllegalArgumentException("Optional type parameters should be specified");
        }
        ResolvedType resolvedType = new TypeResolver().resolve(typeParameters.get(0));
        wrappedHandler = TypeHandler.lookup(resolvedType);
    }

    @Override
    public byte[] getFingerprint() {
        return Bytes.concat("Optional[".getBytes(), wrappedHandler.getFingerprint(), "]".getBytes());
    }

    @Override public int hashCode() {
        return "Optional".hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof OptionalTypeHandler && obj.hashCode() == hashCode();
    }

}
