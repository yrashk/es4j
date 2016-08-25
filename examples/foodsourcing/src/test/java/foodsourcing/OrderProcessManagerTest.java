/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.eventsourcing.cep.events.Deleted;
import com.google.common.io.ByteStreams;
import foodsourcing.commands.AddMenuItem;
import foodsourcing.commands.PlaceOrder;
import foodsourcing.commands.RegisterRestaurant;
import foodsourcing.events.RestaurantRegistered;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class OrderProcessManagerTest extends TestWithRepository {

    private MenuItem spicyHamachiSashimi;
    private MenuItem uniNigiri;

    public OrderProcessManagerTest() {
        super(RegisterRestaurant.class.getPackage(),
              RestaurantRegistered.class.getPackage(),
              Deleted.class.getPackage()
        );
    }

    @Test(timeOut = 10000)
    @SneakyThrows
    public void test() {
        initMenu();
        OrderProcessManager manager = new OrderProcessManager(repository);
        repository.addEntitySubscriber(manager);
        List<Order.Item> items = Arrays
                .asList(new Order.Item(spicyHamachiSashimi, 1), new Order.Item(uniNigiri, 2));
        Order order = repository.publish(new PlaceOrder(items)).get();
        while (order.status() != Order.Status.CONFIRMED) {
            Thread.sleep(100);
        }
    }

    @SneakyThrows
    public void initMenu() {
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        AddMenuItem addSpicyHamachiSashimi = new AddMenuItem(kyzock, "Spicy Hamachi Sashimi",
                                                             "6 small diced cut Hamachi(Fresh Yellow Tail from Japan) sashimi w/ Spicy Sauce & Mix Greens",
                                                             "image/jpeg",
                                                             ByteStreams
                                                                     .toByteArray(getClass().getResourceAsStream("spicy_hamachi_sashimi.jpg")),
                                                             new BigDecimal("8.95"));
        AddMenuItem addUniNigiri = new AddMenuItem(kyzock, "Uni Nigiri", null, "image/jpeg",
                                                   ByteStreams.toByteArray(getClass().getResourceAsStream("uni.jpg")),
                                                   new BigDecimal("3.80"));
        spicyHamachiSashimi = repository.publish(addSpicyHamachiSashimi).get();
        uniNigiri = repository.publish(addUniNigiri).get();
    }

    private RegisterRestaurant kyzockRegistration() {
        Address restaurantAddress = new Address("559 W Pender St, Vancouver, BC", "Canada", "Vancouver",
                                                "V6B 1V5", 49.2837512, -123.1134196);
        return new RegisterRestaurant("Kyzock", restaurantAddress, new OpeningHours(11, 30, 19, 00));
    }
}