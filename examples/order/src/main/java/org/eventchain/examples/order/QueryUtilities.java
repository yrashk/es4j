/**
 * Copyright 2016 Eventchain team
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
package org.eventchain.examples.order;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.EngineThresholds;
import com.googlecode.cqengine.resultset.ResultSet;
import org.eventchain.Entity;
import org.eventchain.EntityHandle;
import org.eventchain.Repository;

import java.util.Optional;

import static com.googlecode.cqengine.query.QueryFactory.*;

public interface QueryUtilities {

    default <O extends Entity> Optional<O> last(Repository repository, Class<O> klass, Query<EntityHandle<O>> query, Attribute<EntityHandle<O>, ? extends Comparable> attribute) {
        try (ResultSet<EntityHandle<O>> resultSet = repository.query(klass, query, queryOptions(orderBy(descending(attribute)), applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            }
            return resultSet.iterator().next().get();
        }
    }

}
