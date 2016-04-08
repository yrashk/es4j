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
