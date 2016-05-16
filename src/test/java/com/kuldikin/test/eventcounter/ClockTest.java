package com.kuldikin.test.eventcounter;

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

}
