/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.osgi;

import lombok.SneakyThrows;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

interface BundleClassScanner {
    default <T> List<Class<? extends T>> findSubTypesOf(Class<?> classInABundle, Class<T> superclass) {
        BundleWiring wiring = FrameworkUtil.getBundle(classInABundle).adapt(BundleWiring.class);
        Collection<String> names = wiring
                .listResources("/" + getClass().getPackage().getName().replaceAll("\\.", "/") + "/",
                               "*", BundleWiring.LISTRESOURCES_RECURSE);
        return names.stream().map(new Function<String, Class<?>>() {
            @Override @SneakyThrows
            public Class<?> apply(String name) {
                String n = name.replaceAll("\\.class$", "").replace('/', '.');
                try {
                    return BundleClassScanner.this.getClass().getClassLoader().loadClass(n);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }).filter(c -> c != null).filter(superclass::isAssignableFrom)
                    .map((Function<Class<?>, Class<? extends T>>) aClass -> (Class<? extends T>) aClass)
                    .collect(Collectors.toList());
    }
}
