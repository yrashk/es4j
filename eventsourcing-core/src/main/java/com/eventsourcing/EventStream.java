/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import com.eventsourcing.repository.LockProvider;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * EventStream is a wrapper around <code>Event&#60;Stream&#62;</code> that holds
 * a typed state.
 *
 * It is used for event generation and passing of state from {@link Command#events(Repository, LockProvider)}
 * to {@link Command#result(Object, Repository, LockProvider)}
 *
 * @param <S> state type
 */
public class EventStream<S> {
    @Getter
    private Stream<? extends Event> stream;
    @Getter
    private S state;

    EventStream(S state, Stream<? extends Event> stream) {
        this.state = state;
        this.stream = stream;
    }

    public static class Builder<S>  {

        private final S state;
        private final Stream.Builder<Event> builder = Stream.builder();

        public Builder(S state) {
            this.state = state;
        }

        public void accept(Event event) {
            builder.accept(event);
        }

        public Builder<S> add(Event event) {
            accept(event);
            return this;
        }

        public EventStream<S> build() {
            return new EventStream<>(state, builder.build());
        }
    }

    /**
     * EventStream builder
     *
     * @param state state
     * @param <S> state type
     * @return
     */
    public static <S> Builder<S> builder(S state) {
        return new Builder<>(state);
    }

    /**
     * EventStream builder with state set to <code>null</code>
     *
     * @param <S> state type
     * @return
     */
    public static <S> Builder<S> builder() {
        return new Builder<>(null);
    }

    /**
     * @param <S> state type
     * @return empty event stream with state set to <code>null</code>
     */
    public static <S> EventStream<S> empty() {
        return new EventStream<>(null, Stream.empty());
    }

    /**
     * @param state state
     * @param <S> state type
     * @return empty event stream with a state
     */
    public static <S> EventStream<S> empty(S state) {
        return new EventStream<>(state, Stream.empty());
    }

    /**
     * @param state state
     * @param stream stream of events
     * @param <S> state type
     * @return event stream with a state and a stream
     */
    public static <S> EventStream<S> ofWithState(S state, Stream<? extends Event> stream) {
        return new EventStream<>(state, stream);
    }

    /**
     * @param stream stream of events
     * @param <S> state type
     * @return event stream with a state set to <code>null</code> and a stream
     */
    public static <S> EventStream<S> of(Stream<? extends Event> stream) {
        return new EventStream<>(null, stream);
    }

    /**
     * @param state state
     * @param event event
     * @param <S> state type
     * @return event stream with a state and a stream of one event
     */
    public static <S> EventStream<S> ofWithState(S state, Event event) {
        return new EventStream<>(state, Stream.of(event));
    }

    /**
     * @param event event
     * @param <S> state type
     * @return event stream with a state set to <code>null</code> and a stream of one event
     */
    public static <S> EventStream<S> of(Event event) {
        return new EventStream<>(null, Stream.of(event));
    }

    /**
     * @param state state
     * @param events events
     * @param <S> state type
     * @return event stream with a state and a stream of multiple events
     */
    public static <S> EventStream<S> ofWithState(S state, Event ...events) {
        return new EventStream<>(state, Stream.of(events));
    }

    /**
     *
     * @param events events
     * @param <S> state type
     * @return event stream with a state set to <code>null</code> and a stream of multiple events
     */
    public static <S> EventStream<S> of(Event ...events) {
        return new EventStream<>(null, Stream.of(events));
    }

}
