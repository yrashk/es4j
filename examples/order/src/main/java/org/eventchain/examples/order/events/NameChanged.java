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
package org.eventchain.examples.order.events;

import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eventchain.Event;
import org.eventchain.annotations.Index;
import org.eventchain.hlc.HybridTimestamp;
import org.eventchain.index.SimpleAttribute;

import java.util.UUID;

import static org.eventchain.index.IndexEngine.IndexFeature.*;

@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class NameChanged extends Event {
    @Getter @Setter
    private UUID id;
    @Getter @Setter
    private String name;

    @Index({EQ})
    public static final SimpleAttribute<NameChanged, UUID> REFERENCE_ID = new SimpleAttribute<NameChanged, UUID>("referenceId") {
        public UUID getValue(NameChanged nameChanged, QueryOptions queryOptions) {
            return nameChanged.id();
        }
    };

    @Index({EQ, LT, GT})
    public static final SimpleAttribute<NameChanged, HybridTimestamp> TIMESTAMP = new SimpleAttribute<NameChanged, HybridTimestamp>("timestamp") {
        public HybridTimestamp getValue(NameChanged nameChanged, QueryOptions queryOptions) {
            return nameChanged.timestamp();
        }
    };

}
