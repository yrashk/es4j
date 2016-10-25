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
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

public class ListTypeHandlerTest {

    public List<String> list;

    @Test @SneakyThrows
    public void parameters() {
        ResolvedType type = new TypeResolver().resolve(getClass().getField("list").getGenericType());
        TypeHandler typeHandler = TypeHandler.lookup(type);
        assertTrue(typeHandler instanceof ListTypeHandler);
    }
}