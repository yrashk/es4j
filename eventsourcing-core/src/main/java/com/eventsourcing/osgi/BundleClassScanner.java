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
        Collection<String> names = wiring.listResources("/" + getClass().getPackage().getName().replaceAll("\\.", "/") + "/",
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
        }).filter(c -> c != null).filter(superclass::isAssignableFrom).map((Function<Class<?>, Class<? extends T>>) aClass -> (Class<? extends T>) aClass).collect(Collectors.toList());
    }
}
