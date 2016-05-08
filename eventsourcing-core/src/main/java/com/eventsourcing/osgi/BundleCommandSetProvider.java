/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.osgi;

import com.eventsourcing.Command;
import com.eventsourcing.CommandSetProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides a command set from a given OSGi bundle
 */
public class BundleCommandSetProvider implements BundleClassScanner, CommandSetProvider {

    private final List<Class<? extends Command>> klasses;

    public BundleCommandSetProvider(Class<?> classInABundle) {
        klasses = findSubTypesOf(classInABundle, Command.class);
    }

    @Override
    public Set<Class<? extends Command>> getCommands() {
        return new HashSet<>(klasses);
    }
}
