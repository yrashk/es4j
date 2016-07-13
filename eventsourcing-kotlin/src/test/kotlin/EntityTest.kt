/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import com.eventsourcing.StandardEvent
import com.eventsourcing.index.MemoryIndexEngine
import com.eventsourcing.index.SimpleIndex
import com.eventsourcing.kotlin.KotlinClassAnalyzer
import com.eventsourcing.layout.UseClassAnalyzer
import org.testng.annotations.Test
import kotlin.test.assertTrue

@UseClassAnalyzer(KotlinClassAnalyzer::class)
class TestEntity(val x: String) : StandardEvent() {
    companion object {
        @JvmField var X = SimpleIndex { o: TestEntity, queryOptions -> o.x }
    }
}

class EntityTest {
    @Test fun companionIndexDefinition() {
        val indexEngine = MemoryIndexEngine()
        val indices = indexEngine.getIndices(TestEntity::class.java)
        val iterator = indices.iterator()
        assertTrue { iterator.hasNext() }
    }
}