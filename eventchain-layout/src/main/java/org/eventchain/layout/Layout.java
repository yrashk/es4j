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
package org.eventchain.layout;

import com.fasterxml.classmate.*;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedPrimitiveType;
import com.google.common.io.BaseEncoding;
import lombok.Getter;
import lombok.SneakyThrows;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Layout is a snapshot of a POJO for the purpose of versioning, serialization and deserialization.
 *
 * Class name, property names and property types are used to deterministically calculate POJO's hash (used for versioning).
 *
 * <h5>Property qualification</h5>
 *
 * Only certain properties will be included into the layout. Here's the definitive list of criteria:
 *
 * <li>
 *     <ul>Has a getter (fluent or JavaBean style)</ul>
 *     <ul>Has a setter (fluent or JavaBean style)</ul>
 *     <ul>Doesn't have a {@link LayoutIgnore} annotation attached to either a getter or a setter</ul>
 *     <ul>Must be of a supported type (see {@link TypeHandler#lookup(ResolvedType)})</ul>
 * </li>
 *
 * Inherited properties from superclasses will also be included.
 *
 * @param <T> Bean's class
 */
public class Layout<T> {

    public static final String DIGEST_ALGORITHM = "SHA-256";
    /**
     * Qualified POJO properties. See {@link Layout for definition}
     */
    @Getter
    private final List<Property<T>> properties;

    @Getter
    private final byte[] hash;

    private final Class<T> klass;

    public Class<T> getLayoutClass() {
        return klass;
    }

    /**
     * Creates POJO's class layout
     *
     * @param klass Bean's class
     * @throws IntrospectionException
     */
    public Layout(Class<T> klass) throws IntrospectionException, NoSuchAlgorithmException, IllegalAccessException {
        this(klass, true);
    }

    // This version of the constructor is only meant to be used in tests to
    // build layouts in slightly different ways to facilitate creation of various
    // scenarios
    Layout(Class<T> klass, boolean hashClassName) throws IntrospectionException, NoSuchAlgorithmException, IllegalAccessException {
        this.klass = klass;
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        TypeResolver typeResolver = new TypeResolver();
        ResolvedType klassType = typeResolver.resolve(klass);

        List<Property<T>> props = new ArrayList<>();

        MemberResolver getterResolver = new MemberResolver(typeResolver);
        getterResolver.setMethodFilter(element -> {
            Method member = element.getRawMember();
            if (member.getAnnotation(LayoutIgnore.class) != null) {
                return false;
            }

            ResolvedType resolvedType = typeResolver.resolve(member.getReturnType());

            // Getter (JavaBean)
            boolean beanGetter = member.getParameterCount() == 0 &&
                    !(resolvedType == ResolvedPrimitiveType.voidType()) &&
                    (member.getName().matches("^get[A-Z][A-za-z_0-9]*") ||
                     (resolvedType.isPrimitive() && resolvedType.getErasedType() == Boolean.TYPE && member.getName().matches("^is[A-Z][A-za-z_0-9]*")));

            // Getter (fluent)
            boolean getter = member.getParameterCount() == 0 &&
                    !member.getName().matches("^(get|is)[A-Z][A-za-z_0-9]*") &&
                    !(resolvedType == ResolvedPrimitiveType.voidType());

            return Modifier.isPublic(member.getModifiers()) && !Modifier.isStatic(member.getModifiers()) &&
                    !element.isAbstract() && (beanGetter || getter);
        });
        ResolvedTypeWithMembers getters = getterResolver.resolve(klassType, new LayoutAnnotationConfiguration(), null);

        MemberResolver setterResolver = new MemberResolver(typeResolver);
        setterResolver.setMethodFilter(element -> {
            Method member = element.getRawMember();
            if (member.getAnnotation(LayoutIgnore.class) != null) {
                return false;
            }
            // Setter (JavaBean)
            boolean beanSetter = member.getParameterCount() == 1 &&
                    member.getName().matches("set[A-Z][A-za-z_0-9]*");

            // Setter (fluent)
            boolean setter = member.getParameterCount() == 1 &&
                    member.getReturnType().isAssignableFrom(klass);

            return Modifier.isPublic(member.getModifiers()) && !Modifier.isStatic(member.getModifiers()) &&
                    !element.isAbstract() && (beanSetter || setter);
        });
        ResolvedTypeWithMembers setters = setterResolver.resolve(klassType, new LayoutAnnotationConfiguration(), null);

        for (ResolvedMethod method : getters.getMemberMethods()) {
            String propertyName = Introspector.decapitalize(method.getName().replaceFirst("^(get|is)", ""));
            String capitalizedPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

            Method getter = method.getRawMember();

            Optional<ResolvedMethod> matchingSetter = Arrays.asList(setters.getMemberMethods()).stream().
                    filter(member -> member.getName().contentEquals(propertyName) ||
                            member.getName().contentEquals("set" + capitalizedPropertyName)).findFirst();

            if (matchingSetter.isPresent()) {
                Method setter = matchingSetter.get().getRawMember();

                MethodHandle getterHandler = lookup.unreflect(getter);
                MethodHandle setterHandler = lookup.unreflect(setter);

                Property<T> property = new Property<>(propertyName,
                        method.getReturnType(),
                        TypeHandler.<T>lookup(method.getReturnType()),
                        new BiConsumer<T, Object>() {
                            @Override @SneakyThrows
                            public void accept(T t, Object o) {
                                setterHandler.invoke(t, o);
                            }
                        },
                        new Function<T, Object>() {
                            @Override @SneakyThrows
                            public Object apply(T t) {
                                return getterHandler.invoke(t);
                            }
                        });
                props.add(property);
            }
        }

        // Sort properties lexicographically (by default, they seem to be sorted anyway,
        // however, no such guarantee was found in the documentation upon brief inspection)
        properties = props.stream().
            sorted((x, y) -> x.getName().compareTo(y.getName())).collect(Collectors.toList());

        // Prepare the hash
        MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);

        // It is important to include class name into the hash as there could be situations
        // when POJOs have indistinguishable layouts, and therefore it is impossible to
        // guarantee that we'd pick the right class
        if (hashClassName) {
            digest.update(klass.getName().getBytes());
        }

        for (Property<T> property : properties) {
            digest.update(property.getName().getBytes());
            digest.update(property.getTypeHandler().getFingerprint());
        }

        this.hash = digest.digest();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Layout && Arrays.equals(getHash(), ((Layout) obj).getHash());
    }

    private static class LayoutAnnotationConfiguration extends AnnotationConfiguration {
        @Override
        public AnnotationInclusion getInclusionForClass(Class<? extends Annotation> annotationType) {
            return AnnotationInclusion.DONT_INCLUDE;
        }

        @Override
        public AnnotationInclusion getInclusionForConstructor(Class<? extends Annotation> annotationType) {
            return AnnotationInclusion.DONT_INCLUDE;
        }

        @Override
        public AnnotationInclusion getInclusionForField(Class<? extends Annotation> annotationType) {
            return AnnotationInclusion.DONT_INCLUDE;
        }

        @Override
        public AnnotationInclusion getInclusionForMethod(Class<? extends Annotation> annotationType) {
            return AnnotationInclusion.INCLUDE_AND_INHERIT;
        }

        @Override
        public AnnotationInclusion getInclusionForParameter(Class<? extends Annotation> annotationType) {
            return AnnotationInclusion.DONT_INCLUDE;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append(klass.getName() + " " + BaseEncoding.base16().encode(hash)).append("\n");
        for (Property<T> property : properties) {
            builder.append("    ").append(property.toString()).append("\n");
        }
        return builder.toString();
    }
}
