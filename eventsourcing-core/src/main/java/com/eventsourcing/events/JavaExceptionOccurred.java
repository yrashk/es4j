/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.index.Attribute;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JavaExceptionOccurred extends StandardEvent {

    @Getter
    private final String className;
    @Getter
    private final String message;
    @Getter
    private final List<StackTraceElement> stacktrace;

    public static class StackTraceElement {
        @Getter
        private final String className;
        @Getter
        private final String fileName;
        @Getter
        private final int lineNumber;
        @Getter
        private final String methodName;
        @Getter
        private final boolean nativeMethod;

        public StackTraceElement(String className, String fileName, int lineNumber, String methodName,
                                 boolean nativeMethod) {
            this.className = className;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.methodName = methodName;
            this.nativeMethod = nativeMethod;
        }

        public StackTraceElement(java.lang.StackTraceElement element) {
            this.className = element.getClassName();
            this.fileName = element.getFileName();
            this.lineNumber = element.getLineNumber();
            this.methodName = element.getMethodName();
            this.nativeMethod = element.isNativeMethod();
        }
    }

    public JavaExceptionOccurred(String className, String message,
                                 List<StackTraceElement> stacktrace) {
        this.className = className;
        this.message = message;
        this.stacktrace = stacktrace;
    }

    public JavaExceptionOccurred(Exception t) {
        this.className = t.getClass().getName();
        this.message = t.getMessage();
        this.stacktrace = Arrays.asList(t.getStackTrace()).stream().
                map(StackTraceElement::new).collect(Collectors.toList());
    }

    @Index
    public static Attribute<JavaExceptionOccurred, UUID> ID = new
            SimpleAttribute<JavaExceptionOccurred, UUID>("id") {
                @Override public UUID getValue(JavaExceptionOccurred object, QueryOptions queryOptions) {
                    return object.uuid();
                }
            };

}
