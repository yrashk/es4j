/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
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
public class OSGiEntitiesProvider {

    private BundleTracker<Collection<ServiceRegistration>> tracker;

    @Activate
    protected void activate(ComponentContext context) {
        tracker = new BundleTracker<>(context.getBundleContext(), Bundle.ACTIVE,
                                    new ScannerBundleTracker(context.getBundleContext()));
        tracker.open();
    }

    @Deactivate
    protected void deactivate() {
        tracker.close();
    }


    private static class BundleCommandEventSetProvider implements CommandSetProvider, EventSetProvider {

        private final List<Class<? extends Command>> commands;
        private final List<Class<? extends Event>> events;

        public BundleCommandEventSetProvider(Bundle bundle) {
            commands = findSubTypesOf(bundle, Collections.singleton(Command.class));
            events = findSubTypesOf(bundle, Collections.singleton(Event.class));
        }

        private <T> List<Class<? extends T>> findSubTypesOf(Bundle bundle, Collection<Class<T>> superclasses) {
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

        @Override public Set<Class<? extends Event>> getEvents() {
            return new HashSet<>(events);
        }

        @Override public Set<Class<? extends Command>> getCommands() {
            return new HashSet<>(commands);
        }
    }

    @Slf4j
    private static class ScannerBundleTracker implements BundleTrackerCustomizer<Collection<ServiceRegistration>> {

        private final BundleContext bundleContext;

        public ScannerBundleTracker(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override public Collection<ServiceRegistration> addingBundle(Bundle bundle, BundleEvent event) {
            BundleCommandEventSetProvider bundleCommandEventSetProvider = new BundleCommandEventSetProvider(bundle);

            ArrayList<ServiceRegistration> registrations = new ArrayList<>();
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("bundle", bundle.getSymbolicName());
            if (!bundleCommandEventSetProvider.getCommands().isEmpty()) {
                properties.put("commands", Joiner.on(", ").join(
                        bundleCommandEventSetProvider.getCommands().stream().map(Class::getName).collect(Collectors.toList())));
                log.info("Registering a service for providing commands in {}", bundle.getSymbolicName());
                registrations.add(bundleContext.registerService(CommandSetProvider.class, bundleCommandEventSetProvider,
                                                           properties));
            }
            if (!bundleCommandEventSetProvider.getEvents().isEmpty()) {
                properties.put("events", Joiner.on(", ").join(
                        bundleCommandEventSetProvider.getEvents().stream().map(Class::getName).collect(Collectors.toList())));
                log.info("Registering a service for providing events in {}", bundle.getSymbolicName());
                registrations.add(bundleContext.registerService(EventSetProvider.class, bundleCommandEventSetProvider,
                                                                properties));
            }
            return registrations;
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event,
                                   Collection<ServiceRegistration> registrations) {
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event,
                                  Collection<ServiceRegistration> registrations) {
            if (!registrations.isEmpty()) {
                log.info("Deregistering services for providing commands and events in {}", bundle.getSymbolicName());
                registrations.forEach(ServiceRegistration::unregister);
            }
        }
    }
}