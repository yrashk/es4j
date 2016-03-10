package org.eventchain.layout;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class LayoutTest {

    private static class VisibilityTestBean {
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
        Layout<VisibilityTestBean> layout = new Layout<>(VisibilityTestBean.class);
        List<Property<VisibilityTestBean>> properties = layout.getProperties();
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
    private static class VisibilityTestBeanChained {
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
        Layout<VisibilityTestBeanChained> layout = new Layout<>(VisibilityTestBeanChained.class);
        List<Property<VisibilityTestBeanChained>> properties = layout.getProperties();
        // Properties without a getter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("privateOnlyGetter")));
        // Properties without both a getter and a setter should be ignored
        assertFalse(properties.stream().anyMatch(property -> property.getName().contentEquals("noGetterOrSetter")));
        // Accessible properties should not be ignored
        assertTrue(properties.stream().anyMatch(property -> property.getName().contentEquals("privateGetterAndSetter")));
    }



    private static class NamingTestBean {
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
        Layout<NamingTestBean> layout = new Layout<>(NamingTestBean.class);
        List<Property<NamingTestBean>> properties = layout.getProperties();
        assertTrue(properties.get(0).getName().contentEquals("first"));
        assertTrue(properties.get(1).getName().contentEquals("second"));
        assertTrue(properties.get(2).getName().contentEquals("third"));
    }

    @Test
    @SneakyThrows
    public void accessingProperties() {
        Layout<NamingTestBean> layout = new Layout<>(NamingTestBean.class);
        List<Property<NamingTestBean>> properties = layout.getProperties();

        NamingTestBean namingTestBean = new NamingTestBean();

        // Setting and retrieving values works as expected:

        properties.get(0).set(namingTestBean, 1);
        assertEquals(1, namingTestBean.getFirst());
        assertEquals((Integer)namingTestBean.getFirst(), properties.get(0).get(namingTestBean));

        properties.get(1).set(namingTestBean, "value");
        assertEquals("value", namingTestBean.getSecond());
        assertEquals(namingTestBean.getSecond(), properties.get(1).get(namingTestBean));

        properties.get(2).set(namingTestBean, true);
        assertEquals(true, namingTestBean.isThird());
        assertEquals((Boolean)namingTestBean.isThird(), properties.get(2).get(namingTestBean));

    }


    private static class DigestTestBean1 {
        @Getter @Setter
        private String x;
    }

    private static class DigestTestBean1Name {
        @Getter @Setter
        private String x;
    }

    private static class DigestTestBean1PropName {
        @Getter @Setter
        private String y;
    }

    private static class DigestTestBean1Type {
        @Getter @Setter
        private int x;
    }

    @Test
    @SneakyThrows
    public void hashDifferentClassName() {
        Layout<DigestTestBean1> layout1 = new Layout<>(DigestTestBean1.class);
        Layout<DigestTestBean1Name> layout1Name = new Layout<>(DigestTestBean1Name.class);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashSameContent() {
        Layout<DigestTestBean1> layout1 = new Layout<>(DigestTestBean1.class, false);
        Layout<DigestTestBean1Name> layout1Name = new Layout<>(DigestTestBean1Name.class, false);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentPropName() {
        Layout<DigestTestBean1> layout1 = new Layout<>(DigestTestBean1.class, false);
        Layout<DigestTestBean1PropName> layout1Name = new Layout<>(DigestTestBean1PropName.class, false);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    @Test
    @SneakyThrows
    public void hashDifferentType() {
        Layout<DigestTestBean1> layout1 = new Layout<>(DigestTestBean1.class, false);
        Layout<DigestTestBean1Type> layout1Name = new Layout<>(DigestTestBean1Type.class, false);

        assertNotEquals(layout1, layout1Name);
        assertNotEquals(layout1.getHash(), layout1Name.getHash());
    }

    private static class DigestTestBean1Unboxed {
        @Getter @Setter
        private int x;
    }

    private static class DigestTestBean1Boxed {
        @Getter @Setter
        private Integer x;
    }

    @Test
    @SneakyThrows
    public void hashBoxed() {
        Layout<DigestTestBean1Unboxed> layout1 = new Layout<>(DigestTestBean1Unboxed.class, false);
        Layout<DigestTestBean1Boxed> layout1Name = new Layout<>(DigestTestBean1Boxed.class, false);

        assertEquals(layout1, layout1Name);
        assertEquals(layout1.getHash(), layout1Name.getHash());
    }

}