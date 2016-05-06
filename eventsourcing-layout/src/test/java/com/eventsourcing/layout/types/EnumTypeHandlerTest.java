/**
 * Copyright 2016 Eventsourcing team
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
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.fasterxml.classmate.TypeResolver;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.*;

public class EnumTypeHandlerTest {

    enum A { A, B, C }
    enum A1 { A, B, C }
    enum A2 { C, A, B }

    @Test @SneakyThrows
    public void fingerprintShape() {
        TypeHandler typeHandlerA = TypeHandler.lookup(new TypeResolver().resolve(A.class), null);
        TypeHandler typeHandlerA1 = TypeHandler.lookup(new TypeResolver().resolve(A1.class), null);
        TypeHandler typeHandlerA2 = TypeHandler.lookup(new TypeResolver().resolve(A2.class), null);
        assertTrue(Arrays.equals(typeHandlerA.getFingerprint(), typeHandlerA1.getFingerprint()));
        assertFalse(Arrays.equals(typeHandlerA.getFingerprint(), typeHandlerA2.getFingerprint()));
    }

}