/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.hlc;


import com.google.common.util.concurrent.ServiceManager;
import org.apache.commons.net.ntp.TimeStamp;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.AssertJUnit.assertEquals;

public class NTPServerTimeProviderTest {

    private ServiceManager serviceManager;

    @DataProvider(name = "delays", parallel = true)
    public static Iterator<Object[]> delays() {
        return IntStream.generate(() -> new Random().nextInt(3000)).
                limit(ForkJoinPool.getCommonPoolParallelism() * 10).
                boxed().
                map(i -> new Object[]{i}).
                collect(Collectors.toList()).
                iterator();
    }

    private NTPServerTimeProvider provider;

    @BeforeClass
    public void setup() throws UnknownHostException, ExecutionException, InterruptedException {
        provider = new NTPServerTimeProvider(new String[]{"localhost"}); // use localhost to avoid delays and usage caps
        serviceManager = new ServiceManager(Arrays.asList(provider));
        serviceManager.startAsync().awaitHealthy();
    }

    @AfterClass
    public void teardown() throws ExecutionException, InterruptedException {
        serviceManager.stopAsync().awaitStopped();
    }

    @Test(successPercentage = 99, dataProvider = "delays")
    public void secondsPassed(int delay) throws UnknownHostException, InterruptedException {
        TimeStamp ts1 = provider.getTimestamp();
        Thread.sleep(delay);
        TimeStamp ts2 = provider.getTimestamp();
        long seconds = delay / 1000;
        // Verify that seconds passed were calculated properly
        // since the last time NTP timestamp was fetched. Measuring fractions
        // is pointless as there's a gap between sleeping and requesting the timestamp.
        assertEquals("Delay=" + delay + " time_diff=" + (ts2.getTime() - ts1.getTime()), seconds, (ts2.getTime() - ts1.getTime()) / 1000);
    }

}