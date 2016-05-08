/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.core;

import java.nio.ByteBuffer;

public interface Deserializer<T> {

    /**
     * Deserializes value of type <code>T</code> from a {@link ByteBuffer}'s
     * current position
     *
     * @param buffer ByteBuffer
     * @return value
     */
    T deserialize(ByteBuffer buffer);

}
