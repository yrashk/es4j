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
