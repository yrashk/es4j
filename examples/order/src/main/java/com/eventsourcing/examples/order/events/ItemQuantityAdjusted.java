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
public class ItemQuantityAdjusted extends Event {
    @Getter @Setter @NonNull
    private UUID itemId;

    @Getter @Setter @NonNull
    private Integer quantity;

    @Index({EQ})
    public static final SimpleAttribute<ItemQuantityAdjusted, UUID> ITEM_ID = new SimpleAttribute<ItemQuantityAdjusted, UUID>(
            "itemId") {
        public UUID getValue(ItemQuantityAdjusted itemQuantityAdjusted, QueryOptions queryOptions) {
            return itemQuantityAdjusted.itemId();
        }
    };
}
