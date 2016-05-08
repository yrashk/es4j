/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

public class LayoutTest {

    private interface InterfaceTest {
        String getTest();

        void setTest(String test);
    }

    @Test
    @SneakyThrows
    public void testInterface() {
        Layout<InterfaceTest> layout = new Layout<>(InterfaceTest.class);
        List<Property<InterfaceTest>> properties = layout.getProperties();
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("test")));
    }

    private static class BaseVisibilityTest {
        @Getter @Setter
        private String inherited;
        @Getter @Setter
        private String base;
    }

    private static class VisibilityTest extends BaseVisibilityTest {
        @Getter @SuppressWarnings("unused")
        private String privateOnlyGetter;
        @Getter @Setter
        private String privateGetterAndSetter;
        @SuppressWarnings("unused")
        private String noGetterOrSetter;
        @Getter(onMethod = @__({@LayoutIgnore})) @Setter
        private String ignored;

        @Override @LayoutIgnore
        public String getBase() {
            return super.getBase();
        }

    }

    @Test
    @SneakyThrows
    public void propertyVisibility() {
        Layout<VisibilityTest> layout = new Layout<>(VisibilityTest.class);
        List<Property<VisibilityTest>> properties = layout.getProperties();
        // Inherited
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("inherited")));
        // Inherited properties can be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("base")));
        // LayoutIgnore
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("ignored")));
        // Properties without a getter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("privateOnlyGetter")));
        // Properties without both a getter and a setter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("noGetterOrSetter")));
        // Accessible properties should not be ignored
        assertTrue(
                properties.stream().anyMatch(property -> property.getName().contentEquals("privateGetterAndSetter")));
    }

    @Accessors(fluent = true)
    private static class VisibilityTestChained {
        @Getter @SuppressWarnings("unused")
        private String privateOnlyGetter;
        @Getter @Setter
        private String privateGetterAndSetter;
        @SuppressWarnings("unused")
        private String noGetterOrSetter;
    }

    @Test
    @SneakyThrows
    public void fluentPropertyVisibility() {
        Layout<VisibilityTestChained> layout = new Layout<>(VisibilityTestChained.class);
        List<Property<VisibilityTestChained>> properties = layout.getProperties();
        // Properties without a getter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("privateOnlyGetter")));
        // Properties without both a getter and a setter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("noGetterOrSetter")));
        // Accessible properties should not be ignored
        assertTrue(
                properties.stream().anyMatch(property -> property.getName().contentEquals("privateGetterAndSetter")));
    }

    private static class ReadonlyTest {
        @Getter
        private String getter;
        @Getter @Setter
        private String getterAndSetter;
    }

    @Test(expectedExceptions = IllegalAccessError.class)
    @SneakyThrows
    public void readonly() {
        Layout<ReadonlyTest> layout = new Layout<>(ReadonlyTest.class, true);
        assertTrue(layout.isReadOnly());
        List<Property<ReadonlyTest>> properties = layout.getProperties();
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("getter")));
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("getterAndSetter")));
        Property<ReadonlyTest> getter = properties.stream()
                                                  .filter(property -> property.getName().contentEquals("getter"))
                                                  .findFirst().get();
        getter.set(new ReadonlyTest(), "hello");
    }

    @AllArgsConstructor
    private static class ConstructorTest {
        @Getter
        private String getter1;
        @Getter
        private String getter2;
    }

    @Test(expectedExceptions = IllegalAccessError.class)
    @SneakyThrows
    public void constructor() {
        Layout<ConstructorTest> layout = new Layout<>(ConstructorTest.class);
        assertFalse(layout.isReadOnly());
        List<Property<ConstructorTest>> properties = layout.getProperties();
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("getter1")));
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("getter2")));
        Property<ConstructorTest> getter = properties.stream()
                                                     .filter(property -> property.getName().contentEquals("getter1"))
                                                     .findFirst().get();
        getter.set(new ConstructorTest("test1", "test2"), "hello");
    }

    private static class MismatchedConstructorsTest {
        @Getter
        private String getter1;
        @Getter
        private String getter2;

        public MismatchedConstructorsTest(String a, int b) {}

        public MismatchedConstructorsTest(int a, String b) {}
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SneakyThrows
    public void mismatchedConstructors() {
        new Layout<>(MismatchedConstructorsTest.class);
    }

    private static class NamingTest {
        @Getter @Setter
        private boolean third;
        @Getter @Setter
        private int first;
        @Getter @Setter
        private String second;
    }

    @Test
    @SneakyThrows
    public void lexicographicalSorting() {
        Layout<NamingTest> layout = new Layout<>(NamingTest.class);
        List<Property<NamingTest>> properties = layout.getProperties();
        assertTrue(properties.get(0).getName().contentEquals("first"));
        assertTrue(properties.get(1).getName().contentEquals("second"));
        assertTrue(properties.get(2).getName().contentEquals("third"));
    }

    @Test
    @SneakyThrows
    public void accessingProperties() {
        Layout<NamingTest> layout = new Layout<>(NamingTest.class);
        List<Property<NamingTest>> properties = layout.getProperties();

        NamingTest namingTest = new NamingTest();

        // Setting and retrieving values works as expected:

        properties.get(0).set(namingTest, 1);
        assertEquals(1, namingTest.getFirst());
        assertEquals((Integer) namingTest.getFirst(), properties.get(0).get(namingTest));

        properties.get(1).set(namingTest, "value");
        assertEquals("value", namingTest.getSecond());
        assertEquals(namingTest.getSecond(), properties.get(1).get(namingTest));

        properties.get(2).set(namingTest, true);
        assertEquals(true, namingTest.isThird());
        assertEquals((Boolean) namingTest.isThird(), properties.get(2).get(namingTest));

    }


    private static class DigestTest1 {
        @Getter @Setter
        private String x;
    }

    private static class DigestTest1Name {
        @Getter @Setter
        private String x;
    }

    @LayoutName("com.eventsourcing.layout.LayoutTest$DigestTest1")
    private static class DigestTest1SameName {
        @Getter @Setter
        private String x;
    }

    private static class DigestTest1PropName {
        @Getter @Setter
        private String y;
    }

    private static class DigestTest1Type {
        @Getter @Setter
        private int x;
    }

    @Test
    @SneakyThrows
    public void hashDifferentClassName() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class);
        Layout<DigestTest1Name> layout1Name = new Layout<>(DigestTest1Name.class);
        Layout<DigestTest1SameName> layout1SameName = new Layout<>(DigestTest1SameName.class);

        assertNotEquals(layout1, layout1Name);
        assertFalse(Arrays.equals(layout1.getHash(), layout1Name.getHash()));

        assertEquals(layout1, layout1SameName);
        assertTrue(Arrays.equals(layout1.getHash(), layout1SameName.getHash()));
    }

    @Test
    @SneakyThrows
    public void hashSameContent() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class, false, false);
        Layout<DigestTest1Name> layout1Name = new Layout<>(DigestTest1Name.class, false, false);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentPropName() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class, false, false);
        Layout<DigestTest1PropName> layout1Name = new Layout<>(DigestTest1PropName.class, false, false);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentType() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class, false, false);
        Layout<DigestTest1Type> layout1Name = new Layout<>(DigestTest1Type.class, false, false);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    private static class DigestTest1Unboxed {
        @Getter @Setter
        private int x;
    }

    private static class DigestTest1Boxed {
        @Getter @Setter
        private Integer x;
    }

    @Test
    @SneakyThrows
    public void hashBoxed() {
        Layout<DigestTest1Unboxed> layout1 = new Layout<>(DigestTest1Unboxed.class, false, false);
        Layout<DigestTest1Boxed> layout1Name = new Layout<>(DigestTest1Boxed.class, false, false);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

    private static class ListParametrizedTest1 {
        @Getter @Setter
        private List<Integer> x;

    }

    private static class ListParametrizedTest2 {
        @Getter @Setter
        private List<String> x;
    }

    @Test
    @SneakyThrows
    public void respectsParametrizedList() {
        Layout<ListParametrizedTest1> layout1 = new Layout<>(ListParametrizedTest1.class, false, false);
        Layout<ListParametrizedTest2> layout2 = new Layout<>(ListParametrizedTest2.class, false, false);

        assertNotEquals(layout1, layout2, "Should be different");
        assertFalse(Arrays.equals(layout1.getHash(), layout2.getHash()));
    }

    private static class OptionalParametrizedTest1 {
        @Getter @Setter
        private Optional<Integer> y;

    }

    private static class OptionalParametrizedTest2 {
        @Getter @Setter
        private Optional<String> y;
    }

    @Test
    @SneakyThrows
    public void respectsParametrizedOptional() {
        Layout<OptionalParametrizedTest1> layout1 = new Layout<>(OptionalParametrizedTest1.class, false, false);
        Layout<OptionalParametrizedTest2> layout2 = new Layout<>(OptionalParametrizedTest2.class, false, false);

        assertNotEquals(layout1, layout2, "Should be different");
        assertFalse(Arrays.equals(layout1.getHash(), layout2.getHash()));
    }
}