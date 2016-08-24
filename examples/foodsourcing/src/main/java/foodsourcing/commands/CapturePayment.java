/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.commands;

import com.eventsourcing.EventStream;
import com.eventsourcing.StandardCommand;
import foodsourcing.events.PaymentCaptured;
import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Accessors(fluent = true)
@Value
public class CapturePayment extends StandardCommand<Void, Void> {
    UUID reference;
    BigDecimal amount;

    @Override public EventStream<Void> events() throws Exception {
        return EventStream.of(new PaymentCaptured(reference, amount));
    }
}
