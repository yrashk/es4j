/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class AddressTest {

    @Test
    public void geocoding() {
        Address address = new Address("559 W Pender St, Vancouver, BC");
        if (Address.geoApiContext != null) {
            assertEquals(address.country(), "Canada");
            assertEquals(address.city(), "Vancouver");
            assertNotEquals(address.latitude(), 0);
            assertNotEquals(address.longitude(), 0);
        }
    }

}