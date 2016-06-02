package com.eventsourcing.examples.order.events;

import com.eventsourcing.Event;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.EQ;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class ItemRemovedFromOrder extends Event {
    @Getter @Setter @NonNull
    private UUID itemId;

    @Index({EQ})
    public static final SimpleAttribute<ItemRemovedFromOrder, UUID> LINE_ID = new SimpleAttribute<ItemRemovedFromOrder, UUID>(
            "itemId") {
        public UUID getValue(ItemRemovedFromOrder itemRemovedFromOrder, QueryOptions queryOptions) {
            return itemRemovedFromOrder.itemId();
        }
    };

}
