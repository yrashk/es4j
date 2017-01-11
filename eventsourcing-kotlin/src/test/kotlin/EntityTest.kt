/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import com.eventsourcing.StandardEvent
import com.eventsourcing.index.JavaStaticFieldIndexLoader
import com.eventsourcing.inmem.MemoryIndexEngine
import com.eventsourcing.index.SimpleIndex
import org.testng.annotations.Test
import kotlin.test.assertTrue

class TestEntity(val x: String) : StandardEvent() {
    companion object {
        @JvmField var X = SimpleIndex.`as` { o: TestEntity -> o.x }
    }
}

class EntityTest {
    @Test fun companionIndexDefinition() {
        val indexEngine = MemoryIndexEngine()
        val indices = JavaStaticFieldIndexLoader().load(indexEngine, TestEntity::class.java)
        val iterator = indices.iterator()
        assertTrue { iterator.hasNext() }
    }
}