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
package org.eventchain.hlc;


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