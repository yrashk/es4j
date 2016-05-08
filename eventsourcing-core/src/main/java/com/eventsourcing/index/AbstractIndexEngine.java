/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
    public <A, O> Index<A> getIndexOnAttribute(Attribute<A, O> attribute, IndexFeature... features)
            throws IndexNotSupported {
        for (IndexCapabilities capabilities : getIndexMatrix()) {
            if (Arrays.asList(capabilities.getFeatures()).containsAll(Arrays.asList(features))) {
                return ((IndexCapabilities<Attribute>) capabilities).getIndex().apply(attribute);
            }
        }
        throw new IndexNotSupported(new Attribute[]{attribute}, features, this);
    }

    @Override @SuppressWarnings("unchecked")
    public <A, O> Index<A> getIndexOnAttributes(Attribute<A, O>[] attributes, IndexFeature... features)
            throws IndexNotSupported {
        for (IndexCapabilities capabilities : getIndexMatrix()) {
            if (Arrays.asList(capabilities.getFeatures()).containsAll(Arrays.asList(features))) {
                return ((IndexCapabilities<Attribute[]>) capabilities).getIndex().apply(attributes);
            }
        }
        throw new IndexNotSupported(attributes, features, this);
    }
}
