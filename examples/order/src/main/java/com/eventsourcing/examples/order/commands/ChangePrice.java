/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.Repository;
import com.eventsourcing.StandardCommand;
import com.eventsourcing.examples.order.events.PriceChanged;
import com.eventsourcing.hlc.HybridTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Accessors(fluent = true)
public class ChangePrice extends StandardCommand<BigDecimal, Void> {
    @Getter
    private final UUID id;

    @Getter
    private final BigDecimal price;

    @Builder
    public ChangePrice(HybridTimestamp timestamp, UUID id, BigDecimal price) {
        super(timestamp);
        this.id = id;
        this.price = price;
    }

    @Override
    public EventStream<Void> events(Repository repository) throws Exception {
        return EventStream.of(PriceChanged.builder().id(id).price(price).build());
    }

    @Override
    public BigDecimal result() {
        return price;
    }
}
