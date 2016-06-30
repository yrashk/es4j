/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import lombok.*;
import org.testng.annotations.Test;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

public class LayoutTest {

    public static class NoConstructor {
        private NoConstructor() {}
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void noConstructor() {
        Layout.forClass(NoConstructor.class);
    }

    public static class ConflictingConstructors {
        public ConflictingConstructors(String a, String b) {
        }
        public ConflictingConstructors(Integer a, String b) {
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void conflictingConstructors() {
        Layout.forClass(ConflictingConstructors.class);
    }

    public static class ExplicitConstructor {
        @Getter private Integer a;
        @Getter private String b;
        public ExplicitConstructor(String a, String b) {
        }
        @LayoutConstructor
        public ExplicitConstructor(Integer a, String b) {
        }
    }

    @Test @SneakyThrows
    public void explicitConstructor() {
        Layout<ExplicitConstructor> layout = Layout.forClass(ExplicitConstructor.class);
        Parameter[] parameters = layout.getConstructor().getParameters();
        assertEquals(parameters[0].getName(), "a");
        assertEquals(parameters[0].getType(), Integer.class);
        assertEquals(parameters[1].getName(), "b");
        assertEquals(parameters[1].getType(), String.class);
    }


    public static class MissingPropertyGetter {
        public MissingPropertyGetter(String a) {
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void missingPropertyGetter() {
        Layout.forClass(MissingPropertyGetter.class);
    }

    public static class MismatchedPropertyType {
        @Getter private Integer a;
        public MismatchedPropertyType(String a) {
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void mismatchedPropertyType() {
        Layout.forClass(MismatchedPropertyType.class);
    }

    public static class RenamedProperty {
        @Getter private String abc;
        public RenamedProperty(@PropertyName("abc") String a) {
        }
    }

    @Test @SneakyThrows
    public void renamedProperty() {
        Layout<RenamedProperty> layout = Layout.forClass(RenamedProperty.class);
        List<Property<RenamedProperty>> properties = layout.getProperties();
        assertEquals(properties.size(), 1);
        assertEquals(properties.get(0).getName(), "abc");
    }

    public static class Properties {
        @Getter private final String a;
        @Getter private final int b;
        @Getter private final boolean c;
        public Properties(boolean c, String a, int b) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    @Test @SneakyThrows
    public void properties() {
        Layout<Properties> layout = Layout.forClass(Properties.class);
        List<Property<Properties>> properties = layout.getProperties();
        assertEquals(properties.size(), 3);
        // Lexicographically sorted:
        assertEquals(properties.get(0).getName(), "a");
        assertEquals(properties.get(1).getName(), "b");
        assertEquals(properties.get(2).getName(), "c");
        // Constructor properties retain the order:
        List<Property<Properties>> constructorProperties = layout.getConstructorProperties();
        assertEquals(constructorProperties.size(), 3);
        assertEquals(constructorProperties.get(0).getName(), "c");
        assertEquals(constructorProperties.get(1).getName(), "a");
        assertEquals(constructorProperties.get(2).getName(), "b");
    }

    @Test @SneakyThrows
    public void getters() {
        Layout<Properties> layout = Layout.forClass(Properties.class);
        List<Property<Properties>> properties = layout.getProperties();
        Properties test = new Properties(true, "hello", 1);
        assertEquals(properties.get(0).get(test), "hello");
        assertEquals((int)properties.get(1).get(test), 1);
        assertEquals((boolean)properties.get(2).get(test), true);
    }



    @LayoutName("DigestTest")
    @Value
    private static class DigestTest1 {
        private String x;
    }

    @Value
    private static class DigestTest1Name {
        private String x;
    }

    @LayoutName("DigestTest")
    @Value
    private static class DigestTest1SameName {
        private String x;
    }

    @LayoutName("DigestTest")
    @Value
    private static class DigestTest1PropName {
        private String y;
    }

    @LayoutName("DigestTest")
    @Value
    private static class DigestTest1Type {
        private int x;
    }

    @Test
    @SneakyThrows
    public void hashDifferentClassName() {
        Layout<DigestTest1> layout1 = Layout.forClass(DigestTest1.class);
        Layout<DigestTest1Name> layout1Name = Layout.forClass(DigestTest1Name.class);
        Layout<DigestTest1SameName> layout1SameName = Layout.forClass(DigestTest1SameName.class);

        assertNotEquals(layout1, layout1Name);
        assertFalse(Arrays.equals(layout1.getHash(), layout1Name.getHash()));

        assertEquals(layout1, layout1SameName);
        assertTrue(Arrays.equals(layout1.getHash(), layout1SameName.getHash()));
    }

    @Test
    @SneakyThrows
    public void hashSameContent() {
        Layout<DigestTest1> layout1 = Layout.forClass(DigestTest1.class);
        Layout<DigestTest1SameName> layout1SameName = Layout.forClass(DigestTest1SameName.class);

        assertEquals(layout1, layout1SameName);
        assertEquals(layout1.getHash(), layout1SameName.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentPropName() {
        Layout<DigestTest1> layout1 = Layout.forClass(DigestTest1.class);
        Layout<DigestTest1PropName> layout1Name = Layout.forClass(DigestTest1PropName.class);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentType() {
        Layout<DigestTest1> layout1 = Layout.forClass(DigestTest1.class);
        Layout<DigestTest1Type> layout1Name = Layout.forClass(DigestTest1Type.class);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @LayoutName("boxing")
    @Value
    private static class DigestTest1Unboxed {
        private short a;
        private int b;
        private long c;
        private float d;
        private double e;
        private boolean f;
        private byte g;
    }

    @LayoutName("boxing")
    @Value
    private static class DigestTest1Boxed {
        private Short a;
        private Integer b;
        private Long c;
        private Float d;
        private Double e;
        private Boolean f;
        private Byte g;
    }

    @Test
    @SneakyThrows
    public void hashBoxed() {
        Layout<DigestTest1Unboxed> layout1 = Layout.forClass(DigestTest1Unboxed.class);
        Layout<DigestTest1Boxed> layout1Name = Layout.forClass(DigestTest1Boxed.class);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

    @LayoutName("ListParametrizedTest")
    @Value
    private static class ListParametrizedTest1 {
        private List<Integer> x;

    }

    @LayoutName("ListParametrizedTest")
    @Value
    private static class ListParametrizedTest2 {
        private List<String> x;
    }

    @Test
    @SneakyThrows
    public void respectsParametrizedList() {
        Layout<ListParametrizedTest1> layout1 = Layout.forClass(ListParametrizedTest1.class);
        Layout<ListParametrizedTest2> layout2 = Layout.forClass(ListParametrizedTest2.class);

        assertNotEquals(layout1, layout2, "Should be different");
        assertFalse(Arrays.equals(layout1.getHash(), layout2.getHash()));
    }

    @LayoutName("OptionalParametrizedTest")
    @Value
    private static class OptionalParametrizedTest1 {
        private Optional<Integer> y;

    }

    @LayoutName("OptionalParametrizedTest")
    @Value
    private static class OptionalParametrizedTest2 {
        private Optional<String> y;
    }

    @Test
    @SneakyThrows
    public void respectsParametrizedOptional() {
        Layout<OptionalParametrizedTest1> layout1 = Layout.forClass(OptionalParametrizedTest1.class);
        Layout<OptionalParametrizedTest2> layout2 = Layout.forClass(OptionalParametrizedTest2.class);

        assertNotEquals(layout1, layout2, "Should be different");
        assertFalse(Arrays.equals(layout1.getHash(), layout2.getHash()));
    }

    @Test
    @SneakyThrows
    public void layoutLayout() {
        Layout<Layout> layout = Layout.forClass(Layout.class);
        assertEquals(layout.getName(), "rfc.eventsourcing.com/spec:7/LDL/#Layout");
        assertEquals(layout.getProperties().size(), 2);
        assertTrue(layout.getProperties().stream().anyMatch(p -> p.getName().contentEquals("name")));
        assertTrue(layout.getProperties().stream().anyMatch(p -> p.getName().contentEquals("properties")));
    }

    @Value
    public static class DefaultConstructor {
        private final String a;
        private boolean b;
    }

    @Test
    @SneakyThrows
    public void defaultConstructor() {
        Layout<DefaultConstructor> layout = Layout.forClass(DefaultConstructor.class);
        Object[] args = layout.getDefaultConstructorArguments();
        DefaultConstructor instance = layout.getConstructor().newInstance(args);
        assertEquals(instance.getA(), "");
        assertFalse(instance.isB());
    }
}