/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.hlc.PhysicalTimeProvider;
import com.eventsourcing.index.IndexEngine;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ServiceManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class DispatchingCommandConsumer extends AbstractService implements CommandConsumer {

    private List<CommandConsumer> consumers;
    private ServiceManager serviceManager;

    public DispatchingCommandConsumer(Set<Class<? extends Command>> commands, PhysicalTimeProvider timeProvider, RepositoryImpl repository, Journal journal, IndexEngine indexEngine, LockProvider lockProvider) {
        consumers = new LinkedList<>();
        for (int i = 0; i < ForkJoinPool.getCommonPoolParallelism(); i++) {
            consumers.add(new DisruptorCommandConsumer(commands, timeProvider, repository, journal, indexEngine, lockProvider));
        }
    }

    @Override
    protected void doStart() {
        serviceManager = new ServiceManager(consumers);
        serviceManager.startAsync().awaitHealthy();
        notifyStarted();
    }

    @Override
    protected void doStop() {
        serviceManager.stopAsync().awaitStopped();
        notifyStopped();
    }

    @Override
    public <T, C extends Command<T>> CompletableFuture<T> publish(C command) {
        UUID uuid = command.uuid();
        HashCode hashCode = HashCode.fromBytes(Bytes.concat(Longs.toByteArray(uuid.getMostSignificantBits()), Longs.toByteArray(uuid.getLeastSignificantBits())));
        int bucket = Hashing.consistentHash(hashCode, consumers.size());
        return consumers.get(bucket).publish(command);
    }

}
