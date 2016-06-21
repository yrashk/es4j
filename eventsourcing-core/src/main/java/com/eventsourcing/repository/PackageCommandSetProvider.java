/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.eventsourcing.Command;

import java.util.Set;

/**
 * Provides a command set from a given list of packages
 */
public class PackageCommandSetProvider extends PackageScanner implements CommandSetProvider {

    public PackageCommandSetProvider(Package[] packages) {
        super(packages);
    }

    public PackageCommandSetProvider(Package[] packages, ClassLoader[] classLoaders) {
        super(packages, classLoaders);
    }

    @Override
    public Set<Class<? extends Command>> getCommands() {
       return scan(Command.class);
    }
}
