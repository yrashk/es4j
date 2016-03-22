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
import com.google.common.util.concurrent.ServiceManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eventchain.hlc.PhysicalTimeProvider;
import org.eventchain.index.IndexEngine;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, property = {"journal.target=", "indexEngine.target=", "lockProvider.target=", "package=", "jmx.objectname=org.eventchain:type=repository"})
@Slf4j
public class RepositoryImpl extends AbstractService implements Repository, RepositoryMBean {

    private Journal journal;

    @Getter
    private Set<Class<? extends Command>> commands = new HashSet<>();
    @Getter
    private Set<Class<? extends Event>> events = new HashSet<>();

    private ServiceManager consumersServiceManager;
    private PhysicalTimeProvider timeProvider;
    @Getter
    private IndexEngine indexEngine;
    private ServiceManager services;
    private LockProvider lockProvider;
    private Package pkg;
    private CommandConsumer commandConsumer;

    @Activate
    protected void activate(ComponentContext ctx) {
        if (!isRunning()) {
            setPackage(Package.getPackage((String)ctx.getProperties().get("package")));
            startAsync();
        }
    }

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

        Reflections reflections = pkg == null ? new Reflections() : new Reflections(pkg.getName());
        Predicate<Class<? extends Entity>> classPredicate = klass ->
                Modifier.isPublic(klass.getModifiers()) &&
                        Modifier.isStatic(klass.getModifiers()) &&
                        !Modifier.isAbstract(klass.getModifiers());
        commands = reflections.getSubTypesOf(Command.class).stream().filter(classPredicate).collect(Collectors.toSet());
        events = reflections.getSubTypesOf(Event.class).stream().filter(classPredicate).collect(Collectors.toSet());

        for (Class<? extends Entity> klass : commands) {
            if (configureIndices(klass)) return;
        }

        for (Class<? extends Entity> klass : events) {
            if (configureIndices(klass)) return;
        }

        commandConsumer = new DisruptorCommandConsumer(commands, timeProvider, this, journal, indexEngine, lockProvider);
        commandConsumer.startAsync().awaitRunning();


        services = new ServiceManager(Arrays.asList(journal, indexEngine, lockProvider, timeProvider));
        services.startAsync().awaitHealthy();

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
    @Override
    public void setPackage(Package pkg) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.pkg = pkg;
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


}
