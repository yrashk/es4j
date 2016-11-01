/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEntity;
import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.Index;
import com.eventsourcing.index.SimpleIndex;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.math.BigDecimal;
import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class PriceChanged extends StandardEvent {
    private UUID reference;
    private BigDecimal price;


    public final static SimpleIndex<PriceChanged, UUID> REFERENCE_ID = SimpleIndex.as(PriceChanged::reference);


    @Index({EQ, LT, GT})
    public final static SimpleIndex<PriceChanged, HybridTimestamp> TIMESTAMP = SimpleIndex.as(StandardEntity::timestamp);

}
