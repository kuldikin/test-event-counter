package com.kuldikin.test.eventcounter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kuldikin
 */
public class ClockTest {

    public ClockTest() {
    }

    @Test
    public void testGetTime() {
        final Clock.CustomizableClock clock = new Clock.CustomizableClock(0);

        for (int i = 0; i < 10000; i++) {
            clock.incClock(1000L);
        }
        assertEquals(1000L * 10000L, clock.getTime());
    }

    @Test
    public void testGetTimeMTh() throws InterruptedException {
        final Clock.CustomizableClock clock = new Clock.CustomizableClock(0);

        ExecutorService es = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            es.submit(() -> {
                for (int i1 = 0; i1 < 1000; i1++) {
                    clock.incClock(1000L);
                }
            });
        }
        es.shutdown();
        es.awaitTermination(5, TimeUnit.SECONDS);
        assertEquals(1000L * 10000L, clock.getTime());
    }

}
