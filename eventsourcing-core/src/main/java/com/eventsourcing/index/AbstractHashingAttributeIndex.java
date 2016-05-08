/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.google.common.hash.HashFunction;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;

import java.util.Set;

public abstract class AbstractHashingAttributeIndex<A, O> extends AbstractAttributeIndex<A, O> {

    protected final HashFunction hashFunction;
    protected final int hashSize;

    /**
     * Protected constructor, called by subclasses.
     *
     * @param attribute        The attribute on which the index will be built
     * @param supportedQueries The set of {@link Query} types which the subclass implementation supports
     * @param hashFunction
     */
    protected AbstractHashingAttributeIndex(Attribute<O, A> attribute, Set<Class<? extends Query>> supportedQueries,
                                            HashFunction hashFunction) {
        super(attribute, supportedQueries);
        this.hashFunction = hashFunction;
        hashSize = hashFunction.bits() / 8;
    }

}
