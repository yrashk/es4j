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

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eventchain.hlc.PhysicalTimeProvider;
import org.eventchain.index.IndexEngine;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, property = {"Journal.target=", "IndexEngine.target=", "LockProvider.target=", "jmx.objectname=org.eventchain:type=repository"})
@Slf4j
public class RepositoryImpl extends AbstractService implements Repository, RepositoryMBean {

    private Journal journal;

    @Getter
    private Set<Class<? extends Command>> commands = new HashSet<>();
    @Getter
    private Set<Class<? extends Event>> events = new HashSet<>();

    private PhysicalTimeProvider timeProvider;
    @Getter
    private IndexEngine indexEngine;
    private ServiceManager services;
    private LockProvider lockProvider;
    private CommandConsumer commandConsumer;

    @Activate
    protected void activate(ComponentContext ctx) {
        if (!isRunning()) {
            startAsync();
        }
    }

    private List<Runnable> initialization = new ArrayList<>();

    @Override @SuppressWarnings("unchecked")
    protected void doStart() {
        if (journal == null) {
            notifyFailed(new IllegalStateException("journal == null"));
        }
        if (timeProvider == null) {
            notifyFailed(new IllegalStateException("physicalTimeProvider == null"));
        }
        if (indexEngine == null) {
            notifyFailed(new IllegalStateException("indexEngine == null"));
        }
        if (lockProvider == null) {
            notifyFailed(new IllegalStateException("lockProvider == null"));
        }

        journal.setRepository(this);
        indexEngine.setJournal(journal);
        indexEngine.setRepository(this);

        services = new ServiceManager(Arrays.asList(journal, indexEngine, lockProvider, timeProvider).stream().
                filter(s -> !s.isRunning()).collect(Collectors.toSet()));
        services.startAsync().awaitHealthy();

        initialization.forEach(Runnable::run);
        initialization.clear();

        commandConsumer = new DisruptorCommandConsumer(commands, timeProvider, this, journal, indexEngine, lockProvider);
        commandConsumer.startAsync().awaitRunning();

        notifyStarted();
    }

    private boolean configureIndices(Class<? extends Entity> klass) {
        try {
            indexEngine.getIndices(klass);
        } catch (IndexEngine.IndexNotSupported | IllegalAccessException e) {
            notifyFailed(e);
            return true;
        }
        return false;
    }

    @Override
    protected void doStop() {
        commandConsumer.stopAsync().awaitTerminated();
        services.stopAsync().awaitStopped();
        // Try stopping services that were started beforehand and didn't
        // make it into `services`
        Arrays.asList(journal, indexEngine, lockProvider, timeProvider).stream().
                filter(Service::isRunning).forEach(s -> s.stopAsync().awaitTerminated());
        notifyStopped();
    }

    @Reference
    @Override
    public void setJournal(Journal journal) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.journal = journal;
    }

    @Reference
    @Override
    public void setIndexEngine(IndexEngine indexEngine) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.indexEngine = indexEngine;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @Override
    public void addCommandSetProvider(CommandSetProvider provider) {
        final Set<Class<? extends Command>> newCommands = provider.getCommands();
        Runnable runnable = () -> {
            for (Class<? extends Entity> klass : newCommands) {
                if (configureIndices(klass)) return;
            }
        };
        this.commands.addAll(newCommands);
        if (isRunning()) {
           // apply immediately
            runnable.run();
            journal.onCommandsAdded(newCommands);
        } else {
            initialization.add(runnable);
        }
    }

    @Override
    public void removeCommandSetProvider(CommandSetProvider provider) {
        final Set<Class<? extends Command>> providedCommands = provider.getCommands();
        commands.removeAll(providedCommands);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @Override
    public void addEventSetProvider(EventSetProvider provider) {
        final Set<Class<? extends Event>> newEvents = provider.getEvents();
        Runnable runnable = () -> {
            for (Class<? extends Entity> klass : newEvents) {
                if (configureIndices(klass)) return;
            }
        };
        this.events.addAll(newEvents);
        if (isRunning()) {
            // apply immediately
            runnable.run();
            journal.onEventsAdded(newEvents);
        } else {
            initialization.add(runnable);
        }
    }

    @Override
    public void removeEventSetProvider(EventSetProvider provider) {
        final Set<Class<? extends Event>> providedEvents = provider.getEvents();
        events.removeAll(providedEvents);
    }

    @Reference
    @Override
    public void setPhysicalTimeProvider(PhysicalTimeProvider timeProvider) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.timeProvider = timeProvider;
    }

    @Reference
    @Override
    public void setLockProvider(LockProvider lockProvider) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.lockProvider = lockProvider;
    }

    @Override
    public <T extends Command<C>, C> CompletableFuture<C> publish(T command) {
        return this.commandConsumer.publish(command);
    }


    @Override
    public String[] getInstalledCommands() {
        return commands.stream().map(Class::getName).toArray(String[]::new);
    }

    @Override
    public String[] getInstalledEvents() {
        return events.stream().map(Class::getName).toArray(String[]::new);
    }
}
