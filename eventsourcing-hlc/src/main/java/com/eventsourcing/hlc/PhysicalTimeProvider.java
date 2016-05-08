/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.hlc;

import com.google.common.util.concurrent.Service;

/**
 * PhysicalTimeProvider interface allows connecting different implementations
 * of NTP 64-bit timestamps
 */
public interface PhysicalTimeProvider extends Service {

    /**
     * @return Current timestamp as an NTP 64-bit value.
     */
    long getPhysicalTime();
}
