/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.osgi;

import com.eventsourcing.Event;
import com.eventsourcing.EventSetProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an event set from a given OSGi bundle
 */

public class BundleEventSetProvider implements BundleClassScanner, EventSetProvider {

    private final List<Class<? extends Event>> klasses;

    public BundleEventSetProvider(Class<?> classInABundle) {
        klasses = findSubTypesOf(classInABundle, Event.class);
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        return new HashSet<>(klasses);
    }
}