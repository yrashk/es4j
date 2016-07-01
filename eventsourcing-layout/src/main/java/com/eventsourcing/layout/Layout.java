/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import com.eventsourcing.layout.binary.BinarySerialization;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.io.BaseEncoding;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import java.beans.IntrospectionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Layout is a snapshot of a class for the purpose of versioning, serialization and deserialization.
 * <p>
 * Layout name, property names and property types are used to deterministically calculate hash (used for versioning).
 * <p>
 * <h1>Property qualification</h1>
 * <p>
 * Only certain properties will be included into the layout. Here's the definitive list of criteria:
 * <p>
 * <ul>
 * <li>Has a getter (fluent or JavaBean style)</li>
 * <li>Has a matching parameter in the constructor (same parameter name or same name through {@link PropertyName}
 *     parameter annotation)</li>
 * <li>Must be of a supported type (see {@link TypeHandler#lookup(ResolvedType, AnnotatedType)})</li>
 * </ul>
 *
 * Additionally, inherited properties will be qualified by the following criteria:
 * <ul>
 *     <li>Has both a getter and a setter (fluent or JavaBean style)</li>
 *     <li>Has a matching parameter in the any parent's class constructor (same parameter name or same name through
 *     {@link PropertyName} parameter annotation)</li>
 *     <li>Must be of a supported type (see {@link TypeHandler#lookup(ResolvedType, AnnotatedType)})</li>
 * </ul>
 * <p>
 *
 * @param <T> Bean's class
 */
@LayoutName("rfc.eventsourcing.com/spec:7/LDL/#Layout")
@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:7/LDL/", revision = "Jun 18, 2016")
@Slf4j
public class Layout<T> {

    public static final String DIGEST_ALGORITHM = "SHA-1";
    /**
     * Qualified properties. See {@link Layout} for definition
     */
    @Getter
    private final List<Property<T>> properties;
    @Getter
    private final List<Property<T>> constructorProperties;

    /**
     * Layout name (derived from class or overriden with {@link LayoutName})
     */
    @Getter
    private final String name;

    /**
     * Layout's hash (fingerprint)
     */
    @Getter
    private byte[] hash;

    @Getter
    private Constructor<T> constructor;

    @Getter
    private Class<T> layoutClass;

    private TypeResolver typeResolver;
    private MethodHandles.Lookup methodHandles;

    private Map<String, MethodHandle> setters = new HashMap<>();


    @LayoutConstructor
    public Layout(String name, List<Property<T>> properties) {
        this.name = name;
        this.properties = properties;
        this.constructorProperties = new ArrayList<>();
    }

    /**
     * Creates a Layout for a class. The class MUST define a constructor with properties. If multiple public
     * constructors are defined, one must be chosen with {@link LayoutConstructor}. Otherwise, by default,
     * a preference is given to the widest constructor (the one with most parameters).
     *
     * @param klass         Type
     * @throws IntrospectionException
     * @throws NoSuchAlgorithmException
     * @throws IllegalAccessException
     * @throws com.eventsourcing.layout.TypeHandler.TypeHandlerException
     */
    public static <T>  Layout<T> forClass(Class<T> klass)
            throws TypeHandler.TypeHandlerException, IntrospectionException, NoSuchAlgorithmException,
                   IllegalAccessException {
        return new Layout<>(klass);
    }

    private Layout(Class<T> klass)
            throws IntrospectionException, NoSuchAlgorithmException, IllegalAccessException,
                   TypeHandler.TypeHandlerException {
        typeResolver = new TypeResolver();
        methodHandles = MethodHandles.lookup();

        layoutClass = klass;
        properties = new ArrayList<>();
        constructorProperties = new ArrayList<>();

        constructor = findLayoutConstructor(layoutClass);
        deriveProperties(layoutClass, constructor, false);

        // Prepare the hash
        MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);

        name = klass.isAnnotationPresent(LayoutName.class) ? klass.getAnnotation(LayoutName.class)
                                                                  .value() : klass.getName();

        // It is important to include class name into the hash as there could be situations
        // when POJOs have indistinguishable layouts, and therefore it is impossible to
        // guarantee that we'd pick the right class
        digest.update(name.getBytes());

        for (Property<T> property : properties) {
            digest.update(property.getName().getBytes());
            digest.update(property.getTypeHandler().getFingerprint());
        }

        this.hash = digest.digest();
    }

    private <X> Constructor<X> findLayoutConstructor(Class<X> klass) {
        @SuppressWarnings("unchecked")
        Constructor<?>[] constructors = klass.getConstructors();

        // Must have at least one public constructor
        if (constructors.length == 0) {
            throw new IllegalArgumentException(klass + " doesn't have any public constructors");
        }

        // Prefer wider constructors
        List<Constructor<?>> constructorList = Arrays.asList(constructors);
        constructorList.sort((o1, o2) -> Integer.compare(o2.getParameterCount(), o1.getParameterCount()));

        // Pick the first constructor by default (if there will be only one)
        Constructor<?> constructor = constructorList.get(0);

        boolean ambiguityDetected = false;

        for (Constructor<?> c : constructorList) {
            // If annotated as a layout constructor, pick it, end of story
            if (c.isAnnotationPresent(LayoutConstructor.class)) {
                return (Constructor<X>) c;
            }

            // If a non-annotated constructor of the same width is found,
            // when there's no annotated constructor, it might cause an
            // ambiguity
            if (c != constructor && c.getParameterCount() == constructor.getParameterCount()) {
                ambiguityDetected = true;
            }
        }

        if (ambiguityDetected) {
            throw new IllegalArgumentException(klass + "has more than one constructor with " +
                                               constructor.getParameterCount() +
                                               " parameters and no @LayoutConstructor-annotated constructor");
        }

        return (Constructor<X>) constructor;
    }

    private void deriveProperties(Class<?> klass, Constructor<T> constructor, boolean parentClass)
            throws TypeHandler.TypeHandlerException,
                   IllegalAccessException {
        Parameter[] parameters = constructor.getParameters();
        // Require parameter names
        for (Parameter parameter : parameters) {
            if (parameter.getName().contentEquals("arg" + properties.size()) &&
                    !parameter.isAnnotationPresent(PropertyName.class)) {
                throw new IllegalArgumentException("arg" + properties.size() + " parameter name detected. " +
                                                   "You must run javac with  -parameters argument or " +
                                                   "use @PropertyName annotation");
            }
            String name = parameter.isAnnotationPresent(PropertyName.class) ?
                    parameter.getAnnotation(PropertyName.class).value() : parameter.getName();

            // if there's such property already, skip processing the parameter
            if (getNullableProperty(name) != null) {
                continue;
            }

            Optional<Method> fluent = retrieveGetter(name, parameter.getType());
            String capitalizedName = capitalizeFirstLetter(name);
            Optional<Method> getX = retrieveGetter("get" + capitalizedName, parameter.getType());
            Optional<Method> isX = (parameter.getType() == Boolean.TYPE || parameter.getType() == Boolean.class)
                    ? retrieveGetter("is" + capitalizedName, parameter.getType()) : Optional.empty();
            Optional<Method> fluentSetter = retrieveSetter(name, parameter.getType());
            Optional<Method> setX = retrieveSetter("set" + capitalizedName, parameter.getType());
            // prefer in this order: getX, isX, fluent
            Optional<Optional<Method>> getter = Stream.of(getX, isX, fluent).filter(Optional::isPresent).findFirst();
            if (!getter.isPresent()) {
                throw new IllegalArgumentException("No getter found for " + layoutClass.getName() + "." + name);
            }
            // Not a valid property if it doesn't have a setter and a setter is required
            if (parentClass && !setX.isPresent() && !fluentSetter.isPresent()) {
                continue;
            }

            if (parentClass) {
                Method setterMethod = Stream.of(setX, fluentSetter).filter(Optional::isPresent).findFirst().get().get();
                MethodHandle setterHandler = methodHandles.unreflect(setterMethod);
                setters.put(name, setterHandler);
            }

            Method method = getter.get().get();

            ResolvedType resolvedType = typeResolver.resolve(method.getReturnType());
            MethodHandle getterHandler = methodHandles.unreflect(method);
            Property<T> property = new Property<>(name, resolvedType,
                                                   TypeHandler.lookup(resolvedType, method.getAnnotatedReturnType()),
                                                   new GetterFunction<T>(getterHandler));
            properties.add(property);
            if (!parentClass) {
                constructorProperties.add(property);
            }
        }
        Class superclass = klass.getSuperclass();
        if (superclass != Object.class) {
            Constructor parentConstructor = findLayoutConstructor(superclass);
            deriveProperties(superclass, parentConstructor, true);
        }
        // Sort properties lexicographically (by default, they seem to be sorted anyway,
        // however, no such guarantee was found in the documentation upon brief inspection)
        properties.sort((x, y) -> x.getName().compareTo(y.getName()));
    }

    private Optional<Method> retrieveGetter(String name, Class<?> type) {
        try {
            Method method = layoutClass.getMethod(name);
            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType() == type)  {
                return Optional.of(method);
            } else {
                return Optional.empty();
            }
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private Optional<Method> retrieveSetter(String name, Class<?> type) {
        try {
            Method method = layoutClass.getMethod(name, type);
            if (Modifier.isPublic(method.getModifiers()))  {
                return Optional.of(method);
            } else {
                return Optional.empty();
            }
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    /**
     * Get a property by name
     * @param name property name
     * @return
     * @throws NoSuchElementException if no such property is defined
     */
    public Property<T> getProperty(String name) throws NoSuchElementException {
        Property<T> property = getNullableProperty(name);
        if (property != null) return property;
        throw new NoSuchElementException();
    }

    private Property<T> getNullableProperty(String name) {
        for (Property<T> property : properties) {
            if (property.getName().contentEquals(name)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Instantiate the layout class with default properties
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public T instantiate() throws Throwable {
        return instantiate(new HashMap<>());
    }

    /**
     * Instantiate the layout class with fully or partially supplied property values
     * @param properties property values
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public T instantiate(Map<Property<T>, Object> properties)
            throws Throwable {
        Object[] args = new Object[constructor.getParameterCount()];
        BinarySerialization serialization = BinarySerialization.getInstance();
        for (int i = 0; i < args.length; i++) {
            Property<T> property = this.constructorProperties.get(i);
            Optional<Object> suppliedProperty = findProperty(properties, property.getName());
            if (suppliedProperty.isPresent()) {
                args[i] = suppliedProperty.get();
            } else {
                TypeHandler typeHandler = property.getTypeHandler();
                ByteBuffer buffer = serialization.getSerializer(typeHandler).serialize(typeHandler, args[i]);
                buffer.rewind();
                Object o = serialization.getDeserializer(typeHandler).deserialize(typeHandler, buffer);
                args[i] = o;
            }
        }
        T t = constructor.newInstance(args);
        if (!setters.isEmpty()) {
            for (Map.Entry<String, MethodHandle> entry : setters.entrySet()) {
                Optional<Object> suppliedProperty = findProperty(properties, entry.getKey());
                if (suppliedProperty.isPresent()) {
                    entry.getValue().invoke(t, suppliedProperty.get());
                }
            }
        }
        return t;
    }

    private Optional<Object> findProperty(Map<Property<T>, Object> properties, String name) {
        for (Map.Entry<Property<T>, Object> entry : properties.entrySet()) {
            if (entry.getKey().getName().contentEquals(name)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Layout && Arrays.equals(getHash(), ((Layout) obj).getHash());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append(
                layoutClass.getName() + " " + BaseEncoding.base16().encode(hash))
                                                   .append("\n");
        for (Property<T> property : properties) {
            builder.append("    ").append(property.toString()).append("\n");
        }
        return builder.toString();
    }

    private static class GetterFunction<T> implements Function<T, Object> {
        private final MethodHandle getterHandler;

        public GetterFunction(MethodHandle getterHandler) {this.getterHandler = getterHandler;}

        @Override
        @SneakyThrows
        public Object apply(T t) {
            return getterHandler.invoke(t);
        }
    }
}
