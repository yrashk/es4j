/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.utils;

import com.googlecode.cqengine.index.support.CloseableIterator;

import java.util.Iterator;

public class CloseableWrappingIterator<T> implements CloseableIterator<T> {

    private final Iterator<T> iterator;

    public CloseableWrappingIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }
}
