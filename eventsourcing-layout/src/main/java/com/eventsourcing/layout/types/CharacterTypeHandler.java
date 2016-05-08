/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;

import java.nio.ByteBuffer;
import java.util.Optional;

public class CharacterTypeHandler implements TypeHandler<Character> {

    private static final Optional<Integer> SIZE = Optional.of(2);

    @Override
    public byte[] getFingerprint() {
        return "Character".getBytes();
    }

    @Override
    public int size(Character value) {
        return 2;
    }

    @Override
    public Optional<Integer> constantSize() {
        return SIZE;
    }

    @Override
    public void serialize(Character value, ByteBuffer buffer) {
        buffer.putChar(value == null ? 0 : value);
    }

    @Override
    public Character deserialize(ByteBuffer buffer) {
        return buffer.getChar();
    }
}
