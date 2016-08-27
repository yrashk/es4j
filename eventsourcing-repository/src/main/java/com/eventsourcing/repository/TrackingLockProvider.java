/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Lock;
import com.eventsourcing.LockProvider;
import com.google.common.util.concurrent.AbstractService;

import java.util.HashSet;
import java.util.Set;

class TrackingLockProvider extends AbstractService implements LockProvider {

    private final Set<Lock> locks = new HashSet<>();
    private final LockProvider lockProvider;

    TrackingLockProvider(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    void release() {
        for (Lock lock : locks) {
            lock.unlock();
        }
    }

    @Override
    public Lock lock(Object lock) {
        Lock l = lockProvider.lock(lock);
        locks.add(l);
        return new TrackingLock(l);
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }

    class TrackingLock implements Lock {

        private final Lock lock;

        public TrackingLock(Lock lock) {
            this.lock = lock;
        }

        @Override
        public void unlock() {
            lock.unlock();
            locks.remove(lock);
        }

        @Override
        public boolean isLocked() {
            return lock.isLocked();
        }
    }
}
