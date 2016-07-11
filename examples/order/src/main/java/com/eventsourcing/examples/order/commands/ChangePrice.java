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
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Accessors(fluent = true)
public class ChangePrice extends StandardCommand<Void, BigDecimal> {
    @Getter
    private final UUID id;

    @Getter
    private final BigDecimal price;

    @Builder
    public ChangePrice(UUID id, BigDecimal price) {
        this.id = id;
        this.price = price;
    }

    @Override
    public EventStream<Void> events() throws Exception {
        return EventStream.of(PriceChanged.builder().id(id).price(price).build());
    }

    @Override
    public BigDecimal result() {
        return price;
    }
}
