/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
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
     *  @param attribute        The attribute on which the index will be built
     * @param supportedQueries The set of {@link Query} types which the subclass implementation supports
     * @param hashFunction
     */
    protected AbstractHashingAttributeIndex(Attribute<O, A> attribute, Set<Class<? extends Query>> supportedQueries, HashFunction hashFunction) {
        super(attribute, supportedQueries);
        this.hashFunction = hashFunction;
        hashSize = hashFunction.bits() / 8;
    }

}
