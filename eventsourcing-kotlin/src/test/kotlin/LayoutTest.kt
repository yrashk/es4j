/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.kotlin

import com.eventsourcing.layout.Layout
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestClass(val x: String)

class LayoutTest {
  @Test fun kotlinAnalyzer() {
      val layout = Layout.forClass(TestClass::class.java)
      assertEquals(layout.properties.size, 1)
      val p = layout.getProperty("x")
      assertNotNull(p)
      assertEquals(p.get(TestClass("a")), "a")
  }
}
