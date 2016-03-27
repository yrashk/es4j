/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain;

import com.google.common.util.concurrent.AbstractService;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Local, in-memory lock provider
 */
@Component(property = {"type=org.eventchain.MemoryLockProvider"})
public class MemoryLockProvider extends AbstractService implements LockProvider {
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

