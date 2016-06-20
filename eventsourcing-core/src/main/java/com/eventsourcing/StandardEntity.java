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
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Standard {@link Entity} implementation. Will generate UUID if one is not provided.
 * @param <E>
 */
public abstract class StandardEntity<E extends Entity> implements Entity<E> {

    private static LinkedBlockingDeque<UUID> uuids = new LinkedBlockingDeque<>(10_000);

    static {
        new Thread(() -> {
            while (true) {
                try {
                    uuids.put(UUID.randomUUID());
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    private UUID uuid;

    /**
     * Returns entity UUID. Generates one if none assigned.
     *
     * @return Entity UUID
     */
    @Override @LayoutIgnore
    public UUID uuid() {
        while (uuid == null) {
            try {
                uuid = uuids.take();
            } catch (InterruptedException e) {
            }
        }
        return uuid;
    }

    private HybridTimestamp timestamp;

    public HybridTimestamp timestamp() {return this.timestamp;}

    public E uuid(UUID uuid) {
        this.uuid = uuid;
        return (E)this;
    }

    public E timestamp(HybridTimestamp timestamp) {
        this.timestamp = timestamp;
        return (E)this;
    }
}
