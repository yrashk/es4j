package org.eventchain;

public class MemoryLockProviderTest extends LockProviderTest<MemoryLockProvider> {
    public MemoryLockProviderTest() {
        super(new MemoryLockProvider());
    }
}
