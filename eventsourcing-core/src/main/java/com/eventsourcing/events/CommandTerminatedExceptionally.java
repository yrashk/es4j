/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class CommandTerminatedExceptionally extends StandardEvent {

    @Getter @Setter
    private UUID commandId;
    @Getter @Setter
    private String className;
    @Getter @Setter
    private String message;
    @Getter @Setter
    private List<StackTraceElement> stacktrace;

    public static class StackTraceElement {
        @Getter @Setter
        private String className;
        @Getter @Setter
        private String fileName;
        @Getter @Setter
        private int lineNumber;
        @Getter @Setter
        private String methodName;
        @Getter @Setter
        private boolean nativeMethod;

        public StackTraceElement() {
        }

        public StackTraceElement(java.lang.StackTraceElement element) {
            this.className = element.getClassName();
            this.fileName = element.getFileName();
            this.lineNumber = element.getLineNumber();
            this.methodName = element.getMethodName();
            this.nativeMethod = element.isNativeMethod();
        }
    }

    public CommandTerminatedExceptionally() {
    }

    public CommandTerminatedExceptionally(UUID commandId, Exception t) {
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
