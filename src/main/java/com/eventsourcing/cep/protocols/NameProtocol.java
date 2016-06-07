package com.eventsourcing.cep.protocols;

import com.eventsourcing.EntityHandle;
import com.eventsourcing.Protocol;
import com.eventsourcing.cep.events.NameChanged;
import com.googlecode.cqengine.query.option.EngineThresholds;
import com.googlecode.cqengine.resultset.ResultSet;
import org.unprotocols.coss.RFC;
import org.unprotocols.coss.Raw;

import static com.googlecode.cqengine.query.QueryFactory.*;

@Raw @RFC(url = "http://rfc.eventsourcing.com/spec:3/CEP")
public interface NameProtocol extends Protocol {
    default String name() {
        try (ResultSet<EntityHandle<NameChanged>> resultSet =
                     getRepository().query(NameChanged.class, equal(NameChanged.REFERENCE_ID, id()),
                                      queryOptions(orderBy(descending(NameChanged.TIMESTAMP)),
                                                   applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
            if (resultSet.isEmpty()) {
                return null;
            }
            return resultSet.iterator().next().get().name();
        }
    }
}
