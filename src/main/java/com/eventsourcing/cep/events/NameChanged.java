package com.eventsourcing.cep.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.eventsourcing.layout.LayoutName;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.util.UUID;

/**
 * This event signifies the name change for a referenced instance.
 */
@Accessors(fluent = true)
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
@LayoutName("http://rfc.eventsourcing.com/spec:3/CEP/#NameChanged")
public class NameChanged extends Event {
    @Getter @Setter
    UUID reference;
    @Getter @Setter
    String name;

    public NameChanged() {
    }

    public NameChanged(UUID reference, String name) {
        this.reference = reference;
        this.name = name;
    }

    @Index
    public static SimpleAttribute<NameChanged, UUID> REFERENCE_ID = new SimpleAttribute<NameChanged, UUID>
            ("reference_id") {
        @Override public UUID getValue(NameChanged nameChanged, QueryOptions queryOptions) {
            return nameChanged.reference();
        }
    };

    @Index
    public static SimpleAttribute<NameChanged, HybridTimestamp> TIMESTAMP = new SimpleAttribute<NameChanged, HybridTimestamp>
            ("timestamp") {
        @Override public HybridTimestamp getValue(NameChanged nameChanged, QueryOptions queryOptions) {
            return nameChanged.timestamp();
        }
    };
}
