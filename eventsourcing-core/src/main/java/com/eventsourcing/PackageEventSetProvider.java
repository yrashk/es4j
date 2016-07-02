/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import java.util.Set;

/**
 * Provides an event set from a given list of packages
 */
public class PackageEventSetProvider extends PackageScanner<Event> implements EventSetProvider {


    public PackageEventSetProvider(Package[] packages) {
        super(packages);
    }

    public PackageEventSetProvider(Package[] packages, ClassLoader[] classLoaders) {
        super(packages, classLoaders);
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
       return scan(Event.class);
    }
}
