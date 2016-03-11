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

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeStamp;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * NTPServerTimeProvider is a physical time provider that uses external NTP servers to fetch timestamp
 * periodically (currently hardcoded as 1 minute).
 *
 * By default, NTP servers are:
 *
 * "0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org", "localhost"
 *
 * NTPServerTimeProvider is an EventReducer Service and needs to be started prior
 * to using it as a PhysicalTimeProvider.
 *
 */
@Component
public class NTPServerTimeProvider extends AbstractScheduledService implements PhysicalTimeProvider {


    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private static final String[] DEFAULT_NTP_SERVERS =
            {"0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org", "localhost"};

    private final NTPUDPClient client;
    private final List<InetAddress> servers;

    private TimeStamp timestamp;
    private long nano;

    @Setter
    @Accessors(fluent = true)
    private long delay = 30;
    @Setter @Accessors(fluent = true)
    private TimeUnit delayUnits = TimeUnit.MINUTES;

    /**
     * Creates NTPServerTimeProvider with default NTP servers
     * @throws UnknownHostException Throws UnknownHostException for the first unresolved host, if no hosts were resolvable
     */
    public NTPServerTimeProvider() throws UnknownHostException {
        this(DEFAULT_NTP_SERVERS);
    }

    @Override
    protected void startUp() throws Exception {
        update();
    }

    @Override
    protected void runOneIteration() throws Exception {
        update();
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, delay, delayUnits);
    }

    /**
     * Creates NTPServerTimeProvider with a custom list of NTP server addresses
     * @param ntpServers Array of custom NTP server addresses
     * @throws UnknownHostException Throws UnknownHostException for the first unresolved host, if no hosts were resolvable
     */
    public NTPServerTimeProvider(String[] ntpServers) throws UnknownHostException {
        client = new NTPUDPClient();
        servers = Arrays.asList(ntpServers).stream().map(server -> {
            try {
                return InetAddress.getByName(server);
            } catch (UnknownHostException e) {
                return null;
            }
        }).filter(address -> address != null).collect(Collectors.toList());
        if (servers.isEmpty()) {
            throw new UnknownHostException(ntpServers[0]);
        }
    }

    synchronized private void update() {
        InetAddress server = servers.remove(0);
        try {
            timestamp = client.getTime(server).getMessage().getTransmitTimeStamp();
            nano = System.nanoTime();
            servers.add(0, server); // add back to the beginning
        } catch (IOException e) {
            servers.add(server); // add to the end of the list
        }
    }

    synchronized TimeStamp getTimestamp() {
        long fraction = timestamp.getFraction();
        long seconds = timestamp.getSeconds();
        long nanoTime = System.nanoTime();
        long l = (nanoTime - nano) / 1_000_000_000;
        double v = (nanoTime - nano) / 1_000_000_000.0 - l;
        long i = (long) (v * 1_000_000_000);
        long fraction_ = fraction + i;
        if (fraction_ >= 1_000_000_000) {
            fraction_ -= 1_000_000_000;
            l++;
        }
        return new TimeStamp((seconds + l) << 32 | fraction_);
    }


    @Override
    public long getPhysicalTime() {
        return getTimestamp().ntpValue();
    }

}

