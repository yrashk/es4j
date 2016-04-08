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

import com.google.common.util.concurrent.AbstractService;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractIndexEngine extends AbstractService implements IndexEngine {


    protected abstract List<IndexCapabilities> getIndexMatrix();

    @Override @SuppressWarnings("unchecked")
    public <A, O> Index<A> getIndexOnAttribute(Attribute<A, O> attribute, IndexFeature... features) throws IndexNotSupported {
        for (IndexCapabilities capabilities : getIndexMatrix()) {
            if (Arrays.asList(capabilities.getFeatures()).containsAll(Arrays.asList(features))) {
                return ((IndexCapabilities<Attribute>)capabilities).getIndex().apply(attribute);
            }
        }
        throw new IndexNotSupported(new Attribute[]{attribute}, features, this);
    }

    @Override @SuppressWarnings("unchecked")
    public <A, O> Index<A> getIndexOnAttributes(Attribute<A, O>[] attributes, IndexFeature... features) throws IndexNotSupported {
        for (IndexCapabilities capabilities : getIndexMatrix()) {
            if (Arrays.asList(capabilities.getFeatures()).containsAll(Arrays.asList(features))) {
                return ((IndexCapabilities<Attribute[]>)capabilities).getIndex().apply(attributes);
            }
        }
        throw new IndexNotSupported(attributes, features, this);
    }
}
