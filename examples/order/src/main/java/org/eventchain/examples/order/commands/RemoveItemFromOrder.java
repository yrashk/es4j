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
package org.eventchain.examples.order.commands;

import lombok.*;
import lombok.experimental.Accessors;
import org.eventchain.Command;
import org.eventchain.Event;
import org.eventchain.Repository;
import org.eventchain.examples.order.events.ItemRemovedFromOrder;

import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class RemoveItemFromOrder extends Command<Void> {

    @Getter @Setter @NonNull
    private UUID itemId;

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.of(new ItemRemovedFromOrder(itemId));
    }
}
