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
package org.eventchain.layout.types;

import org.eventchain.layout.TypeHandler;

import java.nio.ByteBuffer;

public class UnknownTypeHandler implements TypeHandler {

    @Override
    public byte[] getFingerprint() {
        return new byte[0];
    }

    @Override
    public int size(Object value) {
        return 0;
    }

    @Override
    public void serialize(Object value, ByteBuffer buffer) {
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        return null;
    }
}
