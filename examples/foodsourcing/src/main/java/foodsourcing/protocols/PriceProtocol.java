/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.protocols;

import com.eventsourcing.Protocol;
import com.eventsourcing.queries.ModelQueries;
import foodsourcing.events.PriceChanged;

import java.math.BigDecimal;

public interface PriceProtocol extends Protocol, ModelQueries {
    default BigDecimal price() {
        PriceChanged priceChanged = latestAssociatedEntity(PriceChanged.class,
                                                             PriceChanged.REFERENCE_ID, PriceChanged.TIMESTAMP)
                .orElse(new PriceChanged(getId(), BigDecimal.ZERO));
        return priceChanged.price();
    }
}
