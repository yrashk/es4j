package org.eventchain;

import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Local, in-memory lock provider
 */
@Component
public class MemoryLockProvider implements LockProvider {
    private Map<Object, Semaphore> locks = new HashMap<>();

    @Override
    public Lock lock(Object lock) {
        Semaphore semaphore = locks.containsKey(lock) ? locks.get(lock) : new Semaphore(1);
        locks.put(lock, semaphore);
        semaphore.acquireUninterruptibly();
        return new MemoryLock(semaphore);
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

