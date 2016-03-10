/**
 * Copyright 2016 Eventchain team
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
package org.eventchain.layout;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class LayoutTest {

    private static class BaseVisibilityTest {
        @Getter @Setter
        private String inherited;

    }

    private static class VisibilityTest extends BaseVisibilityTest {
        @Getter @SuppressWarnings("unused")
        private String privateOnlyGetter;
        @Getter @Setter
        private String privateGetterAndSetter;
        @SuppressWarnings("unused")
        private String noGetterOrSetter;
        @Getter(onMethod=@__({@LayoutIgnore})) @Setter
        private String ignored;

    }

    @Test
    @SneakyThrows
    public void propertyVisibility() {
        Layout<VisibilityTest> layout = new Layout<>(VisibilityTest.class);
        List<Property<VisibilityTest>> properties = layout.getProperties();
        // Inherited
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("inherited")));
        // LayoutIgnore
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("ignored")));
        // Properties without a getter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("privateOnlyGetter")));
        // Properties without both a getter and a setter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("noGetterOrSetter")));
        // Accessible properties should not be ignored
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("privateGetterAndSetter")));
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
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("privateGetterAndSetter")));
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
        assertEquals((Integer)namingTest.getFirst(), properties.get(0).get(namingTest));

        properties.get(1).set(namingTest, "value");
        assertEquals("value", namingTest.getSecond());
        assertEquals(namingTest.getSecond(), properties.get(1).get(namingTest));

        properties.get(2).set(namingTest, true);
        assertEquals(true, namingTest.isThird());
        assertEquals((Boolean)namingTest.isThird(), properties.get(2).get(namingTest));

    }


    private static class DigestTest1 {
        @Getter @Setter
        private String x;
    }

    private static class DigestTest1Name {
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

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashSameContent() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class, false);
        Layout<DigestTest1Name> layout1Name = new Layout<>(DigestTest1Name.class, false);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentPropName() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class, false);
        Layout<DigestTest1PropName> layout1Name = new Layout<>(DigestTest1PropName.class, false);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentType() {
        Layout<DigestTest1> layout1 = new Layout<>(DigestTest1.class, false);
        Layout<DigestTest1Type> layout1Name = new Layout<>(DigestTest1Type.class, false);

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
        Layout<DigestTest1Unboxed> layout1 = new Layout<>(DigestTest1Unboxed.class, false);
        Layout<DigestTest1Boxed> layout1Name = new Layout<>(DigestTest1Boxed.class, false);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

}