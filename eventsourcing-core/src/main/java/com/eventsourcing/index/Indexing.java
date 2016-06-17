/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.annotations.Index;
import lombok.SneakyThrows;
import org.javatuples.Pair;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Indexing {
    /**
     *
     * Find a named attribute in an entity.
     *
     * @param klass Entity class
     * @param name attribute name
     * @param <O>
     * @param <A>
     * @return SimpleAttribute if attribute was found, <code>null</code> otherwise
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <O extends Entity, A> Attribute<O, A> getAttribute(Class<?> klass, String name) {
        Stream<Pair<Index, Attribute>> stream = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(IndexEngine.getIndexableGetters(klass).iterator(),
                                                            Spliterator.IMMUTABLE), false);
        return (Attribute<O, A>) stream
                .filter(p -> p.getValue1().getAttributeName().equals(name)).findFirst()
                .map(Pair::getValue1)
                .orElse(null);
    }
}
