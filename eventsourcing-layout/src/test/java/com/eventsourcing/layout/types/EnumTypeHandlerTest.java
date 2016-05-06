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