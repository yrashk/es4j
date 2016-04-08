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