/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.google.common.util.concurrent.Service;

import java.util.concurrent.CompletableFuture;

public interface CommandConsumer extends Service {
    <T, C extends Command<T>> CompletableFuture<T> publish(C command);
}
