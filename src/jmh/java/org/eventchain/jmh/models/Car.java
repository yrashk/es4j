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
package org.eventchain.jmh.models;

import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import org.eventchain.Entity;

import java.util.List;
import java.util.UUID;

public class Car extends Entity {
    public String toString() {
        return "org.eventchain.jmh.models.Car(carId=" + this.carId + ", manufacturer=" + this.manufacturer + ", model=" + this.model + ", color=" + this.color + ", doors=" + this.doors + ", price=" + this.price + ", features=" + this.features + ", uuid=" + this.uuid + ")";
    }

    public int getCarId() {
        return this.carId;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public String getModel() {
        return this.model;
    }

    public int getDoors() {
        return this.doors;
    }

    public double getPrice() {
        return this.price;
    }

    public List<String> getFeatures() {
        return this.features;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setDoors(int doors) {
        this.doors = doors;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public enum Color {RED, GREEN, BLUE, BLACK, WHITE}
    int carId;
    String manufacturer;
    String model;
    Color color;
    int doors;
    double price;
    List<String> features;

    private UUID uuid = UUID.randomUUID();

    public Car() {
    }

    public Car(int carId, String manufacturer, String model, Color color, int doors, double price, List<String> features) {
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
