package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.googlecode.cqengine.attribute.*;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;

class WrappedMultiValueIndex<O extends Entity, A> implements MultiValueIndex<O, A> {
    private final MultiValueIndex<O, A> index;
    @Getter @Setter
    private Attribute<O, A> attribute;

    public WrappedMultiValueIndex(MultiValueIndex<O, A> index) {this.index = index;}


    @Override public Iterable<A> getValues(O object) {
        return index.getValues(object);
    }

    @Override public Iterable<A> getValues(O object, QueryOptions queryOptions) {
        return index.getValues(object, queryOptions);
    }
}
