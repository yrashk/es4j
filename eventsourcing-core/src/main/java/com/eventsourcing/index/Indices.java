/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

/**
 * This annotation designates which classes should be scanned for
 * entity's indices. By default, this annotation is not necessary
 * and the entity's own class will be scanned.
 */
public @interface Indices {
    Class<?>[] value();
}
