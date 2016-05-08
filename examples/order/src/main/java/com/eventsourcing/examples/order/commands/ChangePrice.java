/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.examples.order.commands;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;
import com.eventsourcing.examples.order.events.PriceChanged;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class ChangePrice extends Command<BigDecimal> {
    @Getter
    @Setter
    private UUID id;

    @Getter @Setter
    private BigDecimal price;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.of(new PriceChanged(id, price));
    }

    @Override
    public BigDecimal onCompletion() {
        return price;
    }
}
