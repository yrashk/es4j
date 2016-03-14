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
package org.eventchain.h2.index;

import com.googlecode.cqengine.attribute.Attribute;
import org.h2.mvstore.MVStore;

import static org.testng.Assert.*;

public class HashIndexTest extends org.eventchain.index.HashIndexTest<HashIndex> {

    @Override
    public <A, O> HashIndex onAttribute(Attribute<O, A> attribute) {
        return HashIndex.onAttribute(MVStore.open(null), attribute);
    }
}