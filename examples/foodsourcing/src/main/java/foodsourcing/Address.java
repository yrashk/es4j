/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

@ToString
@Accessors(fluent = true)
public class Address {
    @Getter
    private final String address;

    @Getter
    private String country;
    @Getter
    private String city;
    @Getter
    private String postalCode;
    @Getter
    private double latitude;
    @Getter
    private double longitude;

    static GeoApiContext geoApiContext;

    static {
        String googleMapsApiKey = System.getProperty("googleMapsApiKey");
        if (googleMapsApiKey != null) {
            geoApiContext = new GeoApiContext().setApiKey(googleMapsApiKey);
        } else {
            System.out.println("It is recommended to set the googleMapsApiKey property to enable geocoding");
        }

    }

    public static Address DEFAULT_ADDRESS = new Address(null);

    public Address(String address, String country, String city, String postalCode, double latitude, double longitude) {
        this.address = address;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @SneakyThrows
    public Address(String address) {
        this.address = address;
        if (address != null && geoApiContext != null) {
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();
            if (results.length > 0) {
                GeocodingResult result = results[0];
                for (AddressComponent component : result.addressComponents) {
                    List<AddressComponentType> types = Arrays.asList(component.types);
                    if (types.contains(AddressComponentType.COUNTRY)) {
                        this.country = component.longName;
                    }
                    if (types.contains(AddressComponentType.LOCALITY)) {
                        this.city = component.longName;
                    }
                    if (types.contains(AddressComponentType.POSTAL_CODE)) {
                        this.postalCode = component.longName;
                    }
                }
                this.latitude = result.geometry.location.lat;
                this.longitude = result.geometry.location.lng;
            }
        }
    }
}
