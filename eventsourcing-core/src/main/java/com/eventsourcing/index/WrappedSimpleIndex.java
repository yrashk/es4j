package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.googlecode.cqengine.attribute.*;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;

class WrappedSimpleIndex<O extends Entity, A> implements SimpleIndex<O, A> {
    private final SimpleIndex<O, A> index;
    @Getter @Setter
    private Attribute<O, A> attribute;

    public WrappedSimpleIndex(SimpleIndex<O, A> index) {this.index = index;}

    @Override public A getValue(O object) {
        return index.getValue(object);
    }

    @Override public A getValue(O object, QueryOptions queryOptions) {
        return index.getValue(object, queryOptions);
    }
}
