/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.googlecode.cqengine.attribute.*;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eventsourcing.index.EntityQueryFactory.noQueryOptions;

class WrappedSimpleIndex<O extends Entity, A> implements SimpleIndex<O, A>, IndexWithAttribute<O, A> {
    private final BiFunction<O, QueryOptions, A> index;
    @Getter @Setter
    private Attribute<O, A> attribute;

    public WrappedSimpleIndex(Function<O, A> index) {this.index = (e, queryOptions) -> index.apply(e);}
    public WrappedSimpleIndex(BiFunction<O, QueryOptions, A> index) {this.index = index;}

    @Override public A getValue(O object) {
        return index.apply(object, noQueryOptions());
    }

    @Override public A getValue(O object, QueryOptions queryOptions) {
        return index.apply(object, queryOptions);
    }
}
