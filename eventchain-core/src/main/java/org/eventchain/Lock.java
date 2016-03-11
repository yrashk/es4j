package org.eventchain;

/**
 * Simple lock interface to be used for synchronization in Eventchain applications.
 */
public interface Lock {

    /**
     * Unlocks the lock
     */
    void unlock();

    /**
     * @return true if the lock is locked
     */
    @SuppressWarnings("unused")
    boolean isLocked();
}
