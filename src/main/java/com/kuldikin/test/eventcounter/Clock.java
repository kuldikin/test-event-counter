package com.kuldikin.test.eventcounter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 *
 *
 * @author Kuldikin
 */
public abstract class Clock {

    /**
     * Вернет время в мс
     *
     * @return время в мс
     */
    public abstract long getTime();

    private static final Clock DEFAULT = new UserTimeClock();

    /**
     * Часы по умолчанию
     *
     * @return
     */
    public static Clock defaultClock() {
        return DEFAULT;
    }

    /**
     * Системные часы
     */
    public static class UserTimeClock extends Clock {

        @Override
        public long getTime() {
            return System.currentTimeMillis();
        }
    }

    /**
     * Часы с управлением
     */
    public static class CustomizableClock extends Clock {

        private AtomicLong time = new AtomicLong();

        public CustomizableClock(final long time) {
            this.time.addAndGet(time);
        }

        public CustomizableClock() {
            this(System.currentTimeMillis());
        }

        @Override
        public long getTime() {
            return time.longValue();
        }

        public long incClock() {
            return time.incrementAndGet();
        }

        public long incClock(final long inc) {
            return time.addAndGet(inc);
        }

    }

}
