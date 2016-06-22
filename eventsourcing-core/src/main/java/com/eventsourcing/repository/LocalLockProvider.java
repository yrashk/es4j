/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Lock;
import com.eventsourcing.repository.LockProvider;
import com.google.common.util.concurrent.AbstractService;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Local, in-memory lock provider
 */
@Component(property = {"type=MemoryLockProvider"})
public class LocalLockProvider extends AbstractService implements LockProvider {
    private Map<Object, Semaphore> locks = new HashMap<>();

    @Override
    public Lock lock(Object lock) {
        Semaphore semaphore = locks.containsKey(lock) ? locks.get(lock) : new Semaphore(1);
        locks.put(lock, semaphore);
        semaphore.acquireUninterruptibly();
        return new MemoryLock(semaphore);
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }

    static class MemoryLock implements Lock {
        private final Semaphore semaphore;

        public MemoryLock(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void unlock() {
            semaphore.release();
        }

        @Override
        public boolean isLocked() {
            return semaphore.availablePermits() == 0;
        }

    }

}

