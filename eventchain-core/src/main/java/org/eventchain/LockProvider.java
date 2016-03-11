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

import com.google.common.util.concurrent.Service;

import java.util.function.Supplier;

/**
 * Provides a mechanism for locks (see {@link Lock})
 */
public interface LockProvider extends Service {

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
