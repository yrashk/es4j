/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.hlc;

import com.google.common.base.Joiner;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import lombok.SneakyThrows;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JUnitQuickcheck.class)
public class SerializableComparableTest {

    @Property(trials = 1_000)
    public void test(long logicalTime, long logicalCounter, long logicalTime1, long logicalCounter1) {
        HybridTimestamp ts1 = new HybridTimestamp(logicalTime, logicalCounter);
        HybridTimestamp ts2 = new HybridTimestamp(logicalTime1, logicalCounter1);
        assertEquals(ts1.compareTo(ts2), ts1.getSerializableComparable().compareTo(ts2.getSerializableComparable()));
    }

    @Test
    @SneakyThrows
    public void run() {
        JUnitCore junit = new JUnitCore();
        Result result = junit.run(SerializableComparableTest.class);
        if (!result.wasSuccessful()) {
            fail(Joiner.on("\n").join(result.getFailures()));
        }
    }
}
