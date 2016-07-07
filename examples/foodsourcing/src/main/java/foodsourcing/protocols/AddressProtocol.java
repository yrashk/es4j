/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.protocols;

import com.eventsourcing.Protocol;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.queries.ModelQueries;
import foodsourcing.Address;
import foodsourcing.events.AddressChanged;

public interface AddressProtocol extends Protocol, ModelQueries {

    default Address address() {
        AddressChanged addressChanged = latestAssociatedEntity(AddressChanged.class,
                                                               AddressChanged.REFERENCE_ID, AddressChanged.TIMESTAMP)
                                        .orElse(new AddressChanged(getId(), Address.DEFAULT_ADDRESS));
        return addressChanged.address();
    }

    default HybridTimestamp addressRecordedAt() {
        AddressChanged addressChanged = latestAssociatedEntity(AddressChanged.class,
                                                               AddressChanged.REFERENCE_ID, AddressChanged.TIMESTAMP)
                .orElse(new AddressChanged(getId(), Address.DEFAULT_ADDRESS));
        return addressChanged.timestamp();
    }

}
