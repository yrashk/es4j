/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Command;
import com.eventsourcing.hlc.HybridTimestamp;
import com.google.common.util.concurrent.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public interface CommandConsumer extends Service {
    default <T, C extends Command<?, T>> CompletableFuture<T> publish(C command) {
        return publish(command, Collections.emptyList());
    }
    <T, C extends Command<?, T>> CompletableFuture<T> publish(C command, Collection<EntitySubscriber> subscribers);

    HybridTimestamp getTimestamp();
}
