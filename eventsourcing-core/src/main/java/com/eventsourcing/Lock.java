/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

/**
 * Simple lock interface to be used for synchronization in Eventsourcing applications.
 */
public interface Lock {

    /**
     * Unlocks the lock
     */
    void unlock();

    /**
     * @return true if the lock is locked
     */
    @SuppressWarnings("unused") boolean isLocked();
}
