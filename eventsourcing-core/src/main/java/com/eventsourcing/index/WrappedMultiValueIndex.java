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
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eventsourcing.queries.QueryFactory.noQueryOptions;

class WrappedMultiValueIndex<O extends Entity, A, I extends Iterable<A>> implements MultiValueIndex<O, A>,
        IndexWithAttribute<O, A> {
    private final BiFunction<O, QueryOptions, I> index;
    @Getter @Setter
    private Attribute<O, A> attribute;

    public WrappedMultiValueIndex(Function<O, I> index) {this.index = (object, queryOptions) -> index.apply(object);}
    public WrappedMultiValueIndex(BiFunction<O, QueryOptions, I> index) {this.index = index;}


    @Override public Iterable<A> getValues(O object) {
        return index.apply(object, noQueryOptions());
    }

    @Override public Iterable<A> getValues(O object, QueryOptions queryOptions) {
        return index.apply(object, queryOptions);
    }
}
