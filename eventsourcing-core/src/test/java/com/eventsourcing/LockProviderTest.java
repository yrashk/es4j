/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    @SneakyThrows
    public void waiting() {
        Lock lock = lockProvider.lock("test");
        CompletableFuture<Lock> future = new CompletableFuture<>();
        new Thread() {
            @Override public void run() {
                Lock lock1 = lockProvider.lock("test");
                future.complete(lock1);
            }
        }.start();
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
        future.get().unlock();
    }

}