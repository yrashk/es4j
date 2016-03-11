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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eventchain.hlc.HybridTimestamp;
import org.eventchain.layout.LayoutIgnore;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;

public class Entity {

    private static LinkedBlockingDeque<UUID> uuids = new LinkedBlockingDeque<>(1_000_000);

    static {
        ForkJoinPool.commonPool().execute(() -> {
            while (true) {
                try {
                    uuids.put(UUID.randomUUID());
                } catch (InterruptedException e) {
                }
            }
        });
    }

    @Setter @Accessors(fluent = true)
    private UUID uuid;

    @LayoutIgnore
    public UUID uuid() {
        while (uuid == null) {
            try {
                uuid = uuids.take();
            } catch (InterruptedException e) {
            }
        }
        return uuid;
    }

    @Getter(onMethod = @__(@LayoutIgnore)) @Setter @Accessors(fluent = true)
    private HybridTimestamp timestamp;

}
