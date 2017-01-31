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
import foodsourcing.commands.*;
import foodsourcing.events.RestaurantRegistered;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.*;

import static com.eventsourcing.queries.ModelCollectionQuery.LogicalOperators.and;
import static foodsourcing.Restaurant.openAt;
import static foodsourcing.Restaurant.within10km;
import static org.testng.Assert.*;

public class RestaurantTest extends TestWithRepository {

    public RestaurantTest() {
        super(RegisterRestaurant.class.getPackage(),
              RestaurantRegistered.class.getPackage(),
              Deleted.class.getPackage()
              );
    }

    @Test
    @SneakyThrows
    public void initialization() {
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        Map<Integer, List<OpeningHours>> openingHours = kyzock.openingHours();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            assertEquals(openingHours.get(dayOfWeek.getValue()).get(0).from(), new OpeningHours.Time(11, 30));
            assertEquals(openingHours.get(dayOfWeek.getValue()).get(0).till(), new OpeningHours.Time(19, 00));
        }
    }

    @Test
    @SneakyThrows
    public void changeOfHours() {
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        repository.publish(new ChangeRestaurantHours(kyzock, DayOfWeek.FRIDAY,
                                                     Collections.singletonList(new OpeningHours(11, 30, 20, 00)))).get();
        repository.publish(new ChangeRestaurantHours(kyzock, DayOfWeek.SATURDAY, Collections.emptyList())).get();
        repository.publish(new ChangeRestaurantHours(kyzock, DayOfWeek.SUNDAY, Collections.emptyList())).get();

        Map<Integer, List<OpeningHours>> openingHours = kyzock.openingHours();

        assertEquals(openingHours.get(DayOfWeek.FRIDAY.getValue()).get(0).till(), new OpeningHours.Time(20, 00));
        assertTrue(openingHours.get(DayOfWeek.SATURDAY.getValue()).isEmpty());
        assertTrue(openingHours.get(DayOfWeek.SUNDAY.getValue()).isEmpty());
    }

    @Test
    @SneakyThrows
    public void openAtDate() {
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        Date middleOfTheWeekDay = new SimpleDateFormat("MMM dd, yyyy hh:mm").parse("Jul 11, 2016 15:50");
        Collection<Restaurant> restaurants = Restaurant.query(repository, openAt(middleOfTheWeekDay));
        assertEquals(restaurants.size(), 1);
        assertTrue(restaurants.contains(kyzock));
        Date night = new SimpleDateFormat("MMM dd, yyyy hh:mm").parse("Jul 11, 2016 00:00");
        restaurants = Restaurant.query(repository, openAt(night));
        assertEquals(restaurants.size(), 0);
    }

    @Test
    @SneakyThrows
    public void within10Km() {
        Address closeBy = new Address("375 Water St, Vancouver, BC", "Canada", "Vancouver",
                                                "V6B 5C6", 49.2849885, -123.1107973);
        Address outside = new Address("810 Quayside Dr, New Westminster, BC", "Canada", "New Westminster",
                                  "V3M 6B9", 49.200145, -122.911488);
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        Collection<Restaurant> restaurants = Restaurant.query(repository, within10km(closeBy));
        assertEquals(restaurants.size(), 1);
        assertTrue(restaurants.contains(kyzock));
        restaurants = Restaurant.query(repository, within10km(outside));
        assertEquals(restaurants.size(), 0);
    }

    @Test
    @SneakyThrows
    public void within10KmAndOpenAtDate() {
        Date middleOfTheWeekDay = new SimpleDateFormat("MMM dd, yyyy hh:mm").parse("Jul 11, 2016 15:50");
        Date night = new SimpleDateFormat("MMM dd, yyyy hh:mm").parse("Jul 11, 2016 00:00");
        Address closeBy = new Address("375 Water St, Vancouver, BC", "Canada", "Vancouver",
                                      "V6B 5C6", 49.2849885, -123.1107973);

        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        Collection<Restaurant> restaurants = Restaurant.query(repository, and(within10km(closeBy), openAt(middleOfTheWeekDay)));
        assertEquals(restaurants.size(), 1);
        assertTrue(restaurants.contains(kyzock));
        restaurants = Restaurant.query(repository, and(within10km(closeBy), openAt(night)));
        assertEquals(restaurants.size(), 0);
    }

    @Test
    @SneakyThrows
    public void within10KmRelocated() {
        Address closeBy = new Address("375 Water St, Vancouver, BC", "Canada", "Vancouver",
                                      "V6B 5C6", 49.2849885, -123.1107973);
        Address outside = new Address("810 Quayside Dr, New Westminster, BC", "Canada", "New Westminster",
                                      "V3M 6B9", 49.200145, -122.911488);
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        Restaurant sushiZeroOne = repository.publish(sushiZeroOneRegistration()).get();
        repository.publish(new UpdateRestaurantAddress(kyzock, outside)).get();
        repository.publish(new UpdateRestaurantAddress(sushiZeroOne, outside)).get();
        Collection<Restaurant> restaurants = Restaurant.query(repository, within10km(closeBy));
        assertEquals(restaurants.size(), 0);
        restaurants = Restaurant.query(repository, within10km(outside));
        assertEquals(restaurants.size(), 2);
        assertTrue(restaurants.contains(kyzock));
        assertTrue(restaurants.contains(sushiZeroOne));
    }

    @Test
    @SneakyThrows
    public void menu() {
        Restaurant kyzock = repository.publish(kyzockRegistration()).get();
        AddMenuItem addSpicyHamachiSashimi = new AddMenuItem(kyzock, "Spicy Hamachi Sashimi",
                                                  "6 small diced cut Hamachi(Fresh Yellow Tail from Japan) sashimi w/ Spicy Sauce & Mix Greens",
                                                  "image/jpeg",
                                                  ByteStreams.toByteArray(getClass().getResourceAsStream("spicy_hamachi_sashimi.jpg")),
                                                  new BigDecimal("8.95"));
        AddMenuItem addUniNigiri = new AddMenuItem(kyzock, "Uni Nigiri", null, "image/jpeg",
                                                  ByteStreams.toByteArray(getClass().getResourceAsStream("uni.jpg")),
                                                  new BigDecimal("3.80"));
        repository.publish(addSpicyHamachiSashimi).get();
        MenuItem uniNigiri = repository.publish(addUniNigiri).get();
        Collection<MenuItem> menu = kyzock.menu();
        assertEquals(menu.size(), 2);
        // Not in season
        repository.publish(new RemoveMenuItem(uniNigiri)).get();
        menu = kyzock.menu();
        assertEquals(menu.size(), 1);
        assertFalse(menu.stream().anyMatch(item -> item.name().contentEquals(uniNigiri.name())));
    }

    private RegisterRestaurant kyzockRegistration() {
        Address restaurantAddress = new Address("559 W Pender St, Vancouver, BC", "Canada", "Vancouver",
                                                "V6B 1V5", 49.2837512, -123.1134196);
        return new RegisterRestaurant("Kyzock", restaurantAddress, new OpeningHours(11, 30, 19, 00));
    }

    private RegisterRestaurant sushiZeroOneRegistration() {
        Address restaurantAddress = new Address("559 W Pender St, Vancouver, BC", "Canada", "Vancouver",
                                                "V6B 1V5", 49.2837512, -123.1134196);
        return new RegisterRestaurant("Sushi Zero One", restaurantAddress, new OpeningHours(11, 30, 19, 00));
    }



}