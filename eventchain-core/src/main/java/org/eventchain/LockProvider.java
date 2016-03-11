package org.eventchain;

import java.util.function.Supplier;

/**
 * Provides a mechanism for locks (see {@link Lock})
 */
public interface LockProvider {

    /**
     * Instantiates a new lock and locks it.
     * @param lock
     * @return new lock
     */
    Lock lock(Object lock);

    default <T>T withLock(Object lock, Supplier<T> supplier) {
        Lock l = lock(lock);
        T t = supplier.get();
        l.unlock();
        return t;
    }
}
