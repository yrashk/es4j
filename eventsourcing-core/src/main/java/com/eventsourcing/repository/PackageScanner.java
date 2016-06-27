/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.repository;

import com.google.common.collect.Sets;
import org.reflections.Configuration;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.AbstractScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class PackageScanner<T> {

    private final String[] packages;
    private final ClassLoader[] classLoaders;

    public PackageScanner(Package[] packages) {
        this(packages, new ClassLoader[]{});
    }

    public PackageScanner(Package[] packages, ClassLoader[] classLoaders) {
        this.packages = Arrays.asList(packages).stream().map(Package::getName).toArray(String[]::new);
        this.classLoaders = classLoaders;
    }
    Set<Class<? extends T>> scan(Class<? extends T> aClass) {
        Configuration configuration = ConfigurationBuilder.build((Object[]) packages).addClassLoaders(classLoaders)
                                                          .addScanners(new AssignableScanner(aClass));
        Reflections reflections = new Reflections(configuration);
        Predicate<Class<? extends T>> classPredicate = klass ->
                Modifier.isPublic(klass.getModifiers()) &&
                        (!klass.isMemberClass() || (klass.isMemberClass() && Modifier
                                .isStatic(klass.getModifiers()))) &&
                        !Modifier.isInterface(klass.getModifiers()) &&
                        !Modifier.isAbstract(klass.getModifiers());
        HashSet<Class<? extends T>> subtypes = Sets.newHashSet(
                ReflectionUtils.forNames(
                        reflections.getStore()
                                   .getAll(AssignableScanner.class.getSimpleName(),
                                           Collections.singletonList(aClass.getName())), classLoaders));
        return subtypes.stream().filter(classPredicate).collect(Collectors.toSet());
    }

    private static class AssignableScanner extends AbstractScanner {

        private final Class klass;

        AssignableScanner(Class klass) {this.klass = klass;}

        @Override public void scan(Object cls) {
            String className = getMetadataAdapter().getClassName(cls);
            Class<?> aClass = ReflectionUtils.forName(className, getConfiguration().getClassLoaders());

            if (acceptResult(klass.getName()) && klass.isAssignableFrom(aClass) && aClass != klass) {
                getStore().put(klass.getName(), className);
            }

        }
    }
}
