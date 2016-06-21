/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.jmh.models;

import com.eventsourcing.StandardEntity;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@ToString
public class Car extends StandardEntity {
    public enum Color {RED, GREEN, BLUE, BLACK, WHITE}

    @Getter @Setter
    int carId;
    @Getter @Setter
    String manufacturer;
    @Getter @Setter
    String model;
    Color color;
    @Getter @Setter
    int doors;
    @Getter @Setter
    double price;
    @Getter @Setter
    List<String> features;

    @Getter @Setter
    private UUID uuid = UUID.randomUUID();

    public Car() {
    }

    public Car(int carId, String manufacturer, String model, Color color, int doors, double price,
               List<String> features) {
        this.carId = carId;
        this.manufacturer = manufacturer;
        this.model = model;
        this.color = color;
        this.doors = doors;
        this.price = price;
        this.features = features;
    }

    public static final SimpleAttribute<Car, Integer> CAR_ID = new SimpleAttribute<Car, Integer>("carId") {
        public Integer getValue(Car car, QueryOptions queryOptions) { return car.carId; }
    };

    public static final SimpleAttribute<Car, String> MANUFACTURER = new SimpleAttribute<Car, String>("manufacturer") {
        public String getValue(Car car, QueryOptions queryOptions) { return car.manufacturer; }
    };

    public static final SimpleAttribute<Car, String> MODEL = new SimpleAttribute<Car, String>("model") {
        public String getValue(Car car, QueryOptions queryOptions) { return car.model; }
    };

    public static final SimpleAttribute<Car, Color> COLOR = new SimpleAttribute<Car, Color>("color") {
        public Color getValue(Car car, QueryOptions queryOptions) { return car.color; }
    };

    public static final SimpleAttribute<Car, Integer> DOORS = new SimpleAttribute<Car, Integer>("doors") {
        public Integer getValue(Car car, QueryOptions queryOptions) { return car.doors; }
    };

    public static final SimpleAttribute<Car, Double> PRICE = new SimpleAttribute<Car, Double>("price") {
        public Double getValue(Car car, QueryOptions queryOptions) { return car.price; }
    };

    public static final MultiValueAttribute<Car, String> FEATURES = new MultiValueAttribute<Car, String>("features") {
        public Iterable<String> getValues(Car car, QueryOptions queryOptions) { return car.features; }
    };
}
