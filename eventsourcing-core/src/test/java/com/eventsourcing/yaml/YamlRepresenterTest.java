package com.eventsourcing.yaml;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.hlc.NTPServerTimeProvider;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.Property;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.UnsignedLongs;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class YamlRepresenterTest {

    private Yaml yaml;

    private NTPServerTimeProvider timeProvider;
    private HybridTimestamp timestamp;
    private YamlRepresenter representer;


    @BeforeClass @SneakyThrows
    public void setup() {
        timeProvider = new NTPServerTimeProvider(new String[]{"localhost"});
        timeProvider.startAsync().awaitRunning();
        timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();
    }

    @BeforeClass
    public void setUp() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        representer = new YamlRepresenter();
        yaml = new Yaml(representer);
    }

    @Test @SneakyThrows
    public void hybridTimestamp() {
        assertEquals(stringToTimestamp(yaml.loadAs(yaml.dump(timestamp), String.class)), timestamp);
    }

    @Test
    public void optional() {
        Map m = yaml.loadAs(yaml.dump(Optional.empty()), Map.class);
        assertTrue(m.isEmpty());
        m = yaml.loadAs(yaml.dump(Optional.of(1)), Map.class);
        assertEquals(m.get("present"), 1);
    }

    @Test
    public void timestamp() {
        Date date = new Date();
        assertEquals(yaml.load(yaml.dump(date)), date.getTime());
    }

    @Test
    public void uuid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(yaml.load(yaml.dump(uuid)), uuid.toString());
    }

    @Test
    public void bytearray() {
        String str = "Hello, world";
        assertEquals(new String(Base64.getDecoder().decode(yaml.loadAs(yaml.dump(str.getBytes()), String.class))), str);
    }

    @Accessors(fluent = true)
    public static class TestEvent extends Event {
        @Getter @Setter
        private Date ts = new Date();
        @Getter @Setter
        private String test = ts.toString();
    }
    @Accessors(fluent = true)
    public static class TestCommand extends Command<Void> {
        @Getter @Setter
        private String value;

        @Override public Stream<Event> events(Repository repository) throws Exception {
            return Stream.of(new TestEvent());
        }
    }

    @Test @SneakyThrows
    public void command() {
        Layout<TestCommand> layout = new Layout<>(TestCommand.class);
        Command test = (Command) new TestCommand().value("test").timestamp(timestamp);
        Map m = yaml.loadAs(yaml.dump(test), Map.class);
        assertTrue(m.containsKey(test.uuid().toString()));
        assertEquals(m.keySet().size(), 1);
        List values = (List) new ArrayList(m.values()).get(0);
        assertEquals(values.get(0), Base64.getEncoder().encodeToString(layout.getHash()));
        assertEquals(stringToTimestamp((String) values.get(1)), timestamp);
        assertEquals(values.get(2), "test");
    }

    @Test @SneakyThrows
    public void event() {
        Layout<TestEvent> layout = new Layout<>(TestEvent.class);
        TestEvent test = (TestEvent) new TestEvent().timestamp(timestamp);
        Map m = yaml.loadAs(yaml.dump(test), Map.class);
        assertTrue(m.containsKey(test.uuid().toString()));
        assertEquals(m.keySet().size(), 1);
        List values = (List) new ArrayList(m.values()).get(0);
        assertEquals(values.get(0), Base64.getEncoder().encodeToString(layout.getHash()));
        assertEquals(values.get(1), test.test);
        assertEquals(stringToTimestamp((String) values.get(2)), test.timestamp());
        assertEquals(values.get(3), test.ts.getTime());
    }

    @Test @SneakyThrows
    public void layoutCollection() {
        representer.getLayouts().clear();
        Layout<TestCommand> layout = new Layout<>(TestCommand.class);
        Command test = (Command) new TestCommand().value("test").timestamp(timestamp);
        yaml.dump(test);
        Set<Layout> layouts = representer.getLayouts();
        assertEquals(layouts.size(), 2);
        layouts.stream().anyMatch(l -> l.equals(layout));
        layouts.stream().anyMatch(new Predicate<Layout>() {
            @SneakyThrows
            @Override public boolean test(Layout l) {return l.equals(new Layout(HybridTimestamp.class));}
        });
    }

    @Test @SneakyThrows
    public void layout() {
        Layout<TestCommand> layout = new Layout<>(TestCommand.class);
        Map m = yaml.loadAs(yaml.dump(layout), Map.class);
        assertTrue(m.containsKey(Base64.getEncoder().encodeToString(layout.getHash())));
        assertEquals(m.keySet().size(), 1);
        List values = (List) new ArrayList(m.values()).get(0);
        assertEquals(values.size(), layout.getProperties().size() + 1);
        assertEquals(values.get(0), layout.getName());
        int i = 0;
        for (Property property : layout.getProperties()) {
            i++;
            Map map = (Map) values.get(i);
            assertNotNull(map.get(property.getName()));
            boolean printable = CharMatcher.ascii().matchesAllOf(new String(property.getTypeHandler().getFingerprint()));
            if (printable) {
                assertEquals(map.get(property.getName()), new String(property.getTypeHandler().getFingerprint()));
            } else {
                assertTrue(Arrays.equals(Base64.getDecoder().decode((String) map.get(property.getName())),
                                         property.getTypeHandler().getFingerprint()));
            }
        }
    }

    private  HybridTimestamp stringToTimestamp(String s) {
        String[] strings = Iterables.toArray(Splitter.on(".").split(s), String.class);
        return new HybridTimestamp(timeProvider,
                                   UnsignedLongs.parseUnsignedLong(strings[0]),
                                   UnsignedLongs.parseUnsignedLong(strings[1]));
    }

}