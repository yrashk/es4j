/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout;

import java.nio.ByteBuffer;

public interface Deserializer<T, H extends TypeHandler> {

    interface RequiresTypeHandler<T, H extends TypeHandler> extends Deserializer<T, H> {
        default T deserialize(ByteBuffer buffer) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Deserializes value of type <code>T</code> from a {@link ByteBuffer}'s
     * current position
     *
     * @param buffer ByteBuffer
     * @return value
     */
    T deserialize(ByteBuffer buffer);

    /**
     * Deserializes value of type <code>T</code> from a {@link ByteBuffer}'s
     * current position
     *
     * @param typeHandler {@link TypeHandler instance}
     * @param buffer ByteBuffer
     * @return value
     */

    default T deserialize(H typeHandler, ByteBuffer buffer) {
        return deserialize(buffer);
    }
}
