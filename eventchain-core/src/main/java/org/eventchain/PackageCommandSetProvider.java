/**
 * Copyright 2016 Eventchain team
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
package org.eventchain;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PackageCommandSetProvider implements CommandSetProvider {

    private final Package pkg;

    public PackageCommandSetProvider(Package pkg) {
        this.pkg = pkg;
    }

    @Override
    public Set<Class<? extends Command>> getCommands() {
        Reflections reflections = pkg == null ? new Reflections() : new Reflections(pkg.getName());
        Predicate<Class<? extends Entity>> classPredicate = klass ->
                Modifier.isPublic(klass.getModifiers()) &&
                        (!klass.isMemberClass() || (klass.isMemberClass() && Modifier.isStatic(klass.getModifiers()))) &&
                        !Modifier.isAbstract(klass.getModifiers());
        return reflections.getSubTypesOf(Command.class).stream().filter(classPredicate).collect(Collectors.toSet());
    }
}
