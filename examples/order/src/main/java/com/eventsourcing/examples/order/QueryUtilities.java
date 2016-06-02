package com.eventsourcing.examples.order;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.Repository;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.EngineThresholds;
import com.googlecode.cqengine.resultset.ResultSet;

import java.util.Optional;

import static com.googlecode.cqengine.query.QueryFactory.*;

public interface QueryUtilities {

    default <O extends Entity> Optional<O> last(Repository repository, Class<O> klass, Query<EntityHandle<O>> query,
                                                Attribute<EntityHandle<O>, ? extends Comparable> attribute) {
        try (ResultSet<EntityHandle<O>> resultSet = repository.query(klass, query,
                                                                     queryOptions(orderBy(descending(attribute)),
                                                                                  applyThresholds(threshold(
                                                                                          EngineThresholds.INDEX_ORDERING_SELECTIVITY,
                                                                                          0.5))))) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            }
            return resultSet.iterator().next().getOptional();
        }
    }

}
