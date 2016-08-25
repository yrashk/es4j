/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.EntitySubscriber;
import com.eventsourcing.Repository;
import foodsourcing.events.OrderConfirmed;
import foodsourcing.events.OrderPlaced;
import foodsourcing.events.PaymentCaptured;
import foodsourcing.events.RestaurantConfirmedOrder;
import lombok.Synchronized;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class OrderProcessManager implements EntitySubscriber<Entity> {

    private final KieSession session;

    public OrderProcessManager(Repository repository) {
        KieContainer container = KieServices.Factory.get().newKieClasspathContainer(getClass().getClassLoader());

        session = container.newKieSession();
        session.insert(repository);
    }

    @Override public boolean matches(Entity entity) {
        return entity instanceof OrderPlaced ||
                entity instanceof RestaurantConfirmedOrder ||
                entity instanceof PaymentCaptured ||
                entity instanceof OrderConfirmed;
    }


    @Synchronized("session")
    @Override public void onEntity(EntityHandle<Entity> entity) {
        Entity e = entity.get();
        session.insert(e);
        session.fireAllRules();
    }

}
