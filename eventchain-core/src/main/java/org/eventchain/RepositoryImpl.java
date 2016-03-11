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
import com.googlecode.cqengine.attribute.Attribute;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eventchain.annotations.Index;
import org.eventchain.hlc.PhysicalTimeProvider;
import org.eventchain.index.IndexEngine;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Component(servicefactory = true)
@Slf4j
public class RepositoryImpl extends AbstractService implements Repository {

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

        Set<CommandConsumer<? extends Command, Object>> consumers =
                commands.stream().map((java.util.function.Function<Class<? extends Command>,
                                       CommandConsumer<? extends Command, Object>>)
                        this::getCommandConsumer).collect(Collectors.toSet());

        if (!consumers.isEmpty()) {
            consumersServiceManager = new ServiceManager(consumers);
            consumersServiceManager.startAsync().awaitHealthy();
        }

        services = new ServiceManager(Arrays.asList(journal, indexEngine, lockProvider, timeProvider));
        services.startAsync().awaitHealthy();

        notifyStarted();
    }

    private boolean configureIndices(Class<? extends Entity> klass) {
        for (Field field : klass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) &&
                Modifier.isPublic(field.getModifiers())) {
                Index annotation = field.getAnnotation(Index.class);
                if (annotation != null) {
                    try {
                        Attribute attr = (Attribute) field.get(null);
                        indexEngine.getIndexOnAttribute(attr, annotation.value());
                    } catch(IllegalAccessException | IndexEngine.IndexNotSupported e){
                        notifyFailed(e);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void doStop() {
        if (consumersServiceManager != null) {
            consumersServiceManager.stopAsync().awaitStopped();
        }

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

    @Override
    public void setPhysicalTimeProvider(PhysicalTimeProvider timeProvider) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.timeProvider = timeProvider;
    }

    @Override
    public void setLockProvider(LockProvider lockProvider) throws IllegalStateException {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        this.lockProvider = lockProvider;
    }
    @Override @SuppressWarnings("unchecked")
    public <T extends Command<C>, C> CompletableFuture<C> publish(T command) {
        return this.getCommandConsumer((Class<T>) command.getClass()).publish(command);
    }

    private Map<Class<? extends Command<?>>, CommandConsumer<?, ?>> consumers = new HashMap<>();

    @SuppressWarnings("unchecked")
    private <T extends Command<C>, C> CommandConsumer<T, C> getCommandConsumer(Class<T> commandClass) {
        if (!commands.contains(commandClass)) {
            throw new IllegalArgumentException();
        }
        CommandConsumer<T, C> consumer = (CommandConsumer<T, C>) consumers.get(commandClass);
        if (consumer == null) {
            consumer = new CommandConsumer<>(commandClass, timeProvider, this, journal, indexEngine, lockProvider);
            consumers.put(commandClass, consumer);
        }
        return consumer;
    }

}
