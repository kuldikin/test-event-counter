package com.kuldikin.test.eventcounter;

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

        private volatile long time;

        public CustomizableClock(long time) {
            this.time = time;
        }

        public CustomizableClock() {
            this.time = System.currentTimeMillis();
        }

        @Override
        public synchronized long getTime() {
            return time;
        }

        public synchronized void incClock() {
            time++;
        }

        public synchronized void incClock(long inc) {
            time = time + inc;
        }

    }

}
