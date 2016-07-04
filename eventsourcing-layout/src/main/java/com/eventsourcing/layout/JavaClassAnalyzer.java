/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

class JavaClassAnalyzer implements ClassAnalyzer {

    static class JavaParameter implements Parameter {

        private final java.lang.reflect.Parameter parameter;

        JavaParameter(java.lang.reflect.Parameter parameter) {
            this.parameter = parameter;
        }

        @Override public String getName() {
            String name = parameter.getName();
            if (!parameter.isNamePresent() &&
                !parameter.isAnnotationPresent(PropertyName.class)) {
                throw new IllegalArgumentException(name + " parameter name detected. " +
                                                           "You must run javac with  -parameters argument or " +
                                                           "use @PropertyName annotation");
            }
            return parameter.isAnnotationPresent(PropertyName.class) ? parameter.getAnnotation(PropertyName.class)
                                                                                .value() : name;
        }

        @Override public Class<?> getType() {
            return parameter.getType();
        }

    }
    static class JavaClassConstructor<X> implements Constructor<X> {

        @Getter
        private java.lang.reflect.Constructor constructor;

        public JavaClassConstructor(java.lang.reflect.Constructor constructor) {
            this.constructor = constructor;
        }

        @Override public boolean isLayoutConstructor() {
            return constructor.isAnnotationPresent(LayoutConstructor.class);
        }

        @Override public Parameter[] getParameters() {
            Parameter[] p = new Parameter[]{};
            return Arrays.asList(constructor.getParameters()).stream()
                         .map(JavaParameter::new).collect(Collectors.toList()).toArray(p);
        }
    }
    @Override public <X> Constructor<X>[] getConstructors(Class<X> klass) {
        return Arrays.asList(klass.getConstructors()).stream()
                     .map(JavaClassConstructor::new).toArray(Constructor[]::new);

    }
}
