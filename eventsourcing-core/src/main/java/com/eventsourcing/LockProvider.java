/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.google.common.util.concurrent.Service;

import java.util.function.Supplier;

/**
 * Provides a mechanism for locks (see {@link Lock})
 */
public interface LockProvider extends Service {

    /**
     * Instantiates a new lock and locks it.
     *
     * @param lock
     * @return new lock
     */
    Lock lock(Object lock);

    default <T> T withLock(Object lock, Supplier<T> supplier) {
        Lock l = lock(lock);
        T t = supplier.get();
        l.unlock();
        return t;
    }
}
