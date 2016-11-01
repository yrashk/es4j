/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Designates a simple (single value) index using a functional interface.
 *
 * Typically, a definition will look like this:
 *
 * <pre>
 * <code>
 * public static SimpleIndex&lt;TestEvent, UUID&gt; REFERENCE_ID = TestEvent::reference;
 * </code>
 * </pre>
 *
 * However, there are cases when accessing {@link QueryOptions} is necessary. This can be achieved
 * using {@link SimpleIndex#withQueryOptions(BiFunction)}:
 *
 * <pre>
 * <code>
 * public static SimpleIndex&lt;TestEvent, UUID&gt; REFERENCE_ID =
 *    SimpleIndex.withQueryOptions((object, queryOptions) -&gt; ...);
 * </code>
 * </pre>
 *
 * @param <O> entity type
 * @param <A> attribute type
 */
@FunctionalInterface
public interface SimpleIndex<O extends Entity, A> extends EntityIndex<O, A> {
    default A getValue(O object, QueryOptions queryOptions) {
        return getValue(object);
    }
    A getValue(O object);

    /**
     * Creates a SimpleIndex
     *
     * @param function
     * @param <O>
     * @param <A>
     * @return
     */
    static <O extends Entity, A> SimpleIndex<O, A> as(Function<O, A> function) {
        return new WrappedSimpleIndex<>(function);
    }

    /**
     * Creates a SimpleIndex with a function that can access {@link QueryOptions}
     *
     * @param function
     * @param <O>
     * @param <A>
     * @return
     */
    static <O extends Entity, A> SimpleIndex<O, A> withQueryOptions(BiFunction<O, QueryOptions, A> function) {
        return new WrappedSimpleIndex<>(function);
    }

    default Iterable<A> getValues(O object, QueryOptions queryOptions) {
        return Collections.singletonList(getValue(object, queryOptions));
    }

    default Attribute<O, A> getAttribute() {
        throw new IllegalStateException("Index " + this + " hasn't been initialized yet");
    }

}
