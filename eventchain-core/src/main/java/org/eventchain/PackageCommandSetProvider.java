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

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides a command set from a given list of packages
 */
public class PackageCommandSetProvider implements CommandSetProvider {

    private final Package[] packages;
    private final ClassLoader[] classLoaders;

    public PackageCommandSetProvider(Package[] packages) {
        this(packages, new ClassLoader[]{});
    }

    public PackageCommandSetProvider(Package[] packages, ClassLoader[] classLoaders) {
        this.packages = packages;
        this.classLoaders = classLoaders;
    }

    @Override
    public Set<Class<? extends Command>> getCommands() {
        Configuration configuration = ConfigurationBuilder.build().
                forPackages(Arrays.asList(packages).stream().map(Package::getName).toArray(String[]::new)).
                addClassLoaders(classLoaders);
        Reflections reflections = new Reflections(configuration);
        Predicate<Class<? extends Entity>> classPredicate = klass ->
                Modifier.isPublic(klass.getModifiers()) &&
                        (!klass.isMemberClass() || (klass.isMemberClass() && Modifier.isStatic(klass.getModifiers()))) &&
                        !Modifier.isAbstract(klass.getModifiers());
        return reflections.getSubTypesOf(Command.class).stream().filter(classPredicate).collect(Collectors.toSet());
    }
}
