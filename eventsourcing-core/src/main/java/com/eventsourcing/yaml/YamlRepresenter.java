package com.eventsourcing.yaml;

import com.eventsourcing.Command;
import com.eventsourcing.Entity;
import com.eventsourcing.Event;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.Layout;
import com.eventsourcing.layout.Property;
import com.eventsourcing.layout.TypeHandler;
import com.eventsourcing.layout.types.UnknownTypeHandler;
import com.google.common.base.CharMatcher;
import com.google.common.primitives.UnsignedLongs;
import lombok.Getter;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.*;
import java.util.function.Consumer;

public class YamlRepresenter extends Representer {

    public static final String TAG_PREFIX = "!";
    @Getter
    private final Set<Layout> layouts = new HashSet<>();

    private final RepresentEntity representEntity;

    public YamlRepresenter() {
        representEntity = new RepresentEntity();
        this.representers.put(HybridTimestamp.class, new RepresentHybridTimestamp());
        this.representers.put(Date.class, new RepresentTimestamp());
        this.representers.put(Optional.class, new RepresentOptional());
        this.representers.put(UUID.class, new RepresentUUID());
        this.representers.put(byte[].class, new RepresentByteArray());

        this.representers.put(Layout.class, new RepresentLayout());
    }

    @Override public Node represent(Object data) {
        if (Entity.class.isAssignableFrom(data.getClass())) {
            return representEntity.representData(data);
        } else {
            return super.represent(data);
        }
    }

    private class RepresentEntity implements Represent {
        @SneakyThrows
        @Override public Node representData(Object data) {
            Entity entity = (Entity) data;
            Layout layout = new Layout(entity.getClass());
            layouts.add(layout);
            HashMap<Object, Object> map = new HashMap<>();
            ArrayList<Object> values = new ArrayList<>();
            values.add(layout.getHash());
            layout.getProperties().forEach(new Consumer<Property>() {
                @Override public void accept(Property property) {
                    TypeHandler typeHandler = property.getTypeHandler();
                    if (typeHandler instanceof UnknownTypeHandler) {
                        layouts.add(((UnknownTypeHandler) typeHandler).getLayout());
                    }
                    values.add(property.get(entity));
                }
            });

            map.put(entity.uuid(), values);
            String type = "unknown";
            if (entity instanceof Command) {
                type = "command";
            } else if (entity instanceof Event) {
                type = "event";
            }
            return representMapping(new Tag(TAG_PREFIX + type), map, false);
        }
    }
    private class RepresentHybridTimestamp implements Represent {

        @Override public Node representData(Object data) {
            HybridTimestamp timestamp = (HybridTimestamp) data;
            return representScalar(Tag.STR,
                    UnsignedLongs.toString(timestamp.getLogicalTime()) + "." +
                    timestamp.getLogicalCounter());
        }
    }

    private class RepresentTimestamp implements Represent {

        @Override public Node representData(Object data) {
            Date timestamp = (Date) data;
            return representScalar(Tag.INT, String.valueOf(timestamp.getTime()));
        }
    }

    private class RepresentLayout implements Represent {

        @Override public Node representData(Object data) {
            Layout layout = (Layout) data;
            layouts.add(layout);
            HashMap<Object, Object> map = new HashMap<>();
            ArrayList<Object> values = new ArrayList<>();
            values.add(layout.getName());
            layout.getProperties().forEach(new Consumer<Property>() {
                @Override public void accept(Property property) {
                    HashMap<Object, Object> prop = new HashMap<>();

                    TypeHandler typeHandler = property.getTypeHandler();
                    if (typeHandler instanceof UnknownTypeHandler) {
                        layouts.add(((UnknownTypeHandler) typeHandler).getLayout());
                    }
                    byte[] fingerprint = typeHandler.getFingerprint();
                    String stringFingerprint = new String(fingerprint);
                    prop.put(property.getName(),
                             CharMatcher.ascii().matchesAllOf(stringFingerprint) ? stringFingerprint :
                                     fingerprint);

                    values.add(prop);
                }
            });

            map.put(layout.getHash(), values);

            return representMapping(Tag.MAP, map, false);
        }
    }

    private class RepresentOptional implements Represent {

        @Override public Node representData(Object data) {
            Optional optional = (Optional) data;
            HashMap<Object, Object> map = new HashMap<>();
            if (optional.isPresent()) {
                map.put("present", optional.get());
            }

            return representMapping(Tag.MAP, map, false);
        }
    }

    private class RepresentUUID implements Represent {
        @Override public Node representData(Object data) {
            UUID uuid = (UUID) data;
            return representScalar(Tag.STR, uuid.toString());
        }
    }

    private class RepresentByteArray implements Represent {
        @Override public Node representData(Object data) {
            byte[] bytes = (byte[]) data;
            return representScalar(Tag.STR, Base64.getEncoder().encodeToString(bytes));
        }
    }
}
