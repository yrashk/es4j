/**
 * Copyright 2016 Eventsourcing team
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
package com.eventsourcing;

import org.testng.annotations.Test;

import java.util.concurrent.*;

import static org.testng.Assert.*;

public abstract class LockProviderTest<T extends LockProvider> {

    private final T lockProvider;

    public LockProviderTest(T lockProvider) {
        this.lockProvider = lockProvider;
    }

    @Test
    public void locking() {
        Lock lock = lockProvider.lock("test");
        assertTrue(lock.isLocked());
        lock.unlock();
        assertFalse(lock.isLocked());
    }

    @Test(timeOut = 2000)
    public void waiting() {
        Lock lock = lockProvider.lock("test");
        CompletableFuture<Void> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            lockProvider.lock("test");
            future.complete(null);
        });
        try {
            future.get(1, TimeUnit.SECONDS);
            fail("Lock wasn't locked");
            return;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
        }
        lock.unlock();
        future.join();
        assertTrue(future.isDone() && !future.isCancelled());
    }

}