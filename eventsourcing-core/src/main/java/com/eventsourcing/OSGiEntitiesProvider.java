/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import lombok.SneakyThrows;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides an event and command sets from OSGi bundles
 */
@Component(immediate = true)
public class OSGiEntitiesProvider implements CommandSetProvider, EventSetProvider {

    private List<Class<? extends Command>> commands = new ArrayList<>();
    private List<Class<? extends Event>> events = new ArrayList<>();
    private BundleTracker<Object> tracker;

    @Activate
    protected void activate(ComponentContext context) {
        tracker = new BundleTracker<>(context.getBundleContext(), Bundle.ACTIVE,
                                    new ScannerBundleTracker());
        tracker.open();
    }

    @Deactivate
    protected void deactivate() {
        tracker.close();
    }


    <T> List<Class<? extends T>> findSubTypesOf(Bundle bundle, Collection<Class<T>> superclasses) {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        Collection<String> names = wiring
                .listResources("/", "*", BundleWiring.LISTRESOURCES_RECURSE);
        return names.stream().map(new Function<String, Class<?>>() {
            @Override @SneakyThrows
            public Class<?> apply(String name) {
                String n = name.replaceAll("\\.class$", "").replace('/', '.');
                try {
                    return bundle.loadClass(n);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    return null;
                }
            }
        }).filter(c -> c != null).filter(c -> superclasses.stream().anyMatch(sc -> sc.isAssignableFrom(c)))
                    .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
                    .map((Function<Class<?>, Class<? extends T>>) aClass -> (Class<? extends T>) aClass)
                    .collect(Collectors.toList());
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        return new HashSet<>(events);
    }

    @Override public Set<Class<? extends Command>> getCommands() {
        return new HashSet<>(commands);
    }

    private class ScannerBundleTracker implements BundleTrackerCustomizer<Object> {
        @Override public Object addingBundle(Bundle bundle,
                                             BundleEvent event) {
            events.addAll(findSubTypesOf(bundle, Arrays.asList(Event.class)));
            commands.addAll(findSubTypesOf(bundle, Arrays.asList(Command.class)));
            return null;
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event,
                                   Object object) {
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event,
                                  Object object) {
            events.removeAll(findSubTypesOf(bundle, Arrays.asList(Event.class)));
            commands.removeAll(findSubTypesOf(bundle, Arrays.asList(Command.class)));
        }
    }
}