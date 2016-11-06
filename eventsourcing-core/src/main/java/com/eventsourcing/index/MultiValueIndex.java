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

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Designates a multiple values index using a functional interface
 *
 * Typically, a definition will look like this:
 *
 * <pre>
 * <code>
 * public static MultiValueIndex&lt;TestEvent, UUID&gt; REFERENCE_ID = (object) -&gt; ...;
 * </code>
 * </pre>
 *
 * However, there are cases when accessing {@link QueryOptions} is necessary. This can be achieved
 * using {@link MultiValueIndex#withQueryOptions(BiFunction)}:
 *
 * <pre>
 * <code>
 * public static MultiValueIndex&lt;TestEvent, UUID&gt; REFERENCE_ID =
 *    MultiValueIndex.withQueryOptions((object, queryOptions) -&gt; ...);
 * </code>
 * </pre>
 *
 * @param <O> entity type
 * @param <A> attribute type
 */
@FunctionalInterface
public interface MultiValueIndex<O extends Entity, A> extends EntityIndex<O, A> {
    default Iterable<A> getValues(O object, QueryOptions queryOptions) {
        return getValues(object);
    }

    Iterable<A> getValues(O object);


    /**
     * Creates a MultiValueIndex with a function that can access {@link QueryOptions}
     * @param function
     * @param <O>
     * @param <A>
     * @return
     */
    static <O extends Entity, A> MultiValueIndex<O, A> as(Function<O, Iterable<A>> function) {
        return new WrappedMultiValueIndex<>(function);
    }


    /**
     * Creates a MultiValueIndex with a function that can access {@link QueryOptions}
     * @param function
     * @param <O>
     * @param <A>
     * @return
     */
    static <O extends Entity, A> MultiValueIndex<O, A>
        withQueryOptions(BiFunction<O, QueryOptions, Iterable<A>> function) {
        return new WrappedMultiValueIndex<>(function);
    }


    default Attribute<O, A> getAttribute() {
        throw new IllegalStateException("Index " + this + " hasn't been initialized yet");
    }
}
