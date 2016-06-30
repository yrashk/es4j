/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class CommandTerminatedExceptionally extends StandardEvent {

    @Getter
    private final UUID commandId;
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

    @Builder
    public CommandTerminatedExceptionally(HybridTimestamp timestamp, UUID commandId, String className,
                                          String message,
                                          List<StackTraceElement> stacktrace) {
        super(timestamp);
        this.commandId = commandId;
        this.className = className;
        this.message = message;
        this.stacktrace = stacktrace;
    }

    public CommandTerminatedExceptionally(UUID commandId, Exception t) {
        super(null);
        this.commandId = commandId;
        this.className = t.getClass().getName();
        this.message = t.getMessage();
        this.stacktrace = Arrays.asList(t.getStackTrace()).stream().
                map(StackTraceElement::new).collect(Collectors.toList());
    }

    public static SimpleAttribute<CommandTerminatedExceptionally, UUID> COMMAND_ID = new SimpleAttribute<CommandTerminatedExceptionally, UUID>(
            "commandId") {
        @Override
        public UUID getValue(CommandTerminatedExceptionally object, QueryOptions queryOptions) {
            return object.commandId();
        }
    };
}
