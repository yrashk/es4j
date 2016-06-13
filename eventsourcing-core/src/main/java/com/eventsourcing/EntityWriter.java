/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public interface EntityWriter {
    default void write(OutputStream stream, Iterable<Entity> iterable) throws IOException {
        write(stream, iterable.iterator());
    }
    void write(OutputStream stream, Iterator<Entity> iterator) throws IOException;
    void writeLayouts(OutputStream stream) throws IOException;
}
