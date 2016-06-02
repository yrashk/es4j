/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.LayoutIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;

public class Entity {

    private static LinkedBlockingDeque<UUID> uuids = new LinkedBlockingDeque<>(10_000);

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

    @Getter @Setter @Accessors(fluent = true)
    private HybridTimestamp timestamp;

}
