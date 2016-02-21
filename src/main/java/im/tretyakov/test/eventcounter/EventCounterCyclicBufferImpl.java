package im.tretyakov.test.eventcounter;

/**
 * Реализация интерфейса для учета однотипных событий в системе
 * с использованием кольцевых буферов.
 * <p>
 * Данная реализация обеспечивает регистрацию более 8.000.000 событий в секунду при одном поставщике.
 * <p>
 * Created on 21.02.16.
 *
 * @author tretyakov (dmitry@tretyakov.im)
 */
public class EventCounterCyclicBufferImpl implements EventCounter {

    private static final long MILLIS_IN_SECOND = 1000L;

    private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000L;

    private final long[] events = new long[86400];

    private volatile int eventsPointer = 0;

    private volatile long lastDayHours = System.currentTimeMillis() / MILLIS_IN_DAY;

    /**
     * Учитывает событие
     */
    @Override
    public synchronized void countEvent() {
        final long millis = System.currentTimeMillis();
        final long currentDayHours = millis / MILLIS_IN_DAY;
        final int currentIndex = (int) (millis / MILLIS_IN_SECOND) % 86400;
        if (currentDayHours == this.lastDayHours) {
            if (currentIndex == this.eventsPointer) {
                this.events[currentIndex]++;
            } else {
                if (currentIndex < this.eventsPointer) {
                    System.arraycopy(
                        new long[this.eventsPointer - currentIndex],
                        0,
                        this.events,
                        currentIndex,
                        this.eventsPointer - currentIndex
                    );
                }
                this.events[currentIndex] = 1L;
                this.eventsPointer = currentIndex;
            }
        } else if (currentDayHours == this.lastDayHours + 1) {
            if (currentIndex == this.eventsPointer) {
                System.arraycopy(new long[86400], 0, this.events, 0, 86400);
            } else if (currentIndex < this.eventsPointer) {
                System.arraycopy(
                    new long[this.eventsPointer - currentIndex],
                    0,
                    this.events,
                    currentIndex, this.eventsPointer - currentIndex
                );
            } else {
                System.arraycopy(new long[86400 - currentIndex], 0, this.events, currentIndex, 86400 - currentIndex);
            }
            this.events[currentIndex] = 1L;
            this.eventsPointer = currentIndex;
        } else {
            System.arraycopy(new long[86400], 0, this.events, 0, 86400);
            this.events[currentIndex] = 1L;
            this.eventsPointer = currentIndex;
        }
        this.lastDayHours = currentDayHours;
    }

    /**
     * Выдаёт число событий за последнюю минуту (60 секунд)
     *
     * @return число событий за последнюю минуту (60 секунд)
     */
    @Override
    public long eventsByLastMinute() {
        final long millis = System.currentTimeMillis();
        final long[] seconds = new long[60];
        final long dayHours = millis / MILLIS_IN_DAY;
        final int currentPointer = (int) ((millis / MILLIS_IN_SECOND) % 86400);
        synchronized (this.events) {
            if (dayHours <= this.lastDayHours + 1) {
                if (currentPointer > 58) {
                    System.arraycopy(this.events, currentPointer - 59, seconds, 0, 60);
                } else {
                    System.arraycopy(this.events, 86399 - (58 - currentPointer), seconds, 0, 59 - currentPointer);
                    System.arraycopy(this.events, 0, seconds, 59 - currentPointer, currentPointer + 1);
                }
            } else {
                return 0L;
            }
        }
        long sum = 0L;
        for (long events : seconds) {
            sum += events;
        }
        return sum;
    }

    /**
     * Выдаёт число событий за последний час (60 минут)
     *
     * @return число событий за последний час (60 минут)
     */
    @Override
    public long eventsByLastHour() {
        final long millis = System.currentTimeMillis();
        final long[] minutes = new long[3600];
        final long dayHours = millis / MILLIS_IN_DAY;
        final int currentPointer = (int) ((millis / MILLIS_IN_SECOND) % 86400);
        synchronized (this.events) {
            if (dayHours <= this.lastDayHours + 1) {
                if (currentPointer > 3598) {
                    System.arraycopy(this.events, currentPointer - 3599, minutes, 0, 3600);
                } else {
                    System.arraycopy(this.events, 86399 - (3598 - currentPointer), minutes, 0, 3599 - currentPointer);
                    System.arraycopy(this.events, 0, minutes, 3599 - currentPointer, currentPointer + 1);
                }
            } else {
                return 0L;
            }
        }
        long sum = 0L;
        for (long events : minutes) {
            sum += events;
        }
        return sum;
    }

    /**
     * Выдаёт число событий за последние сутки (24 часа)
     *
     * @return число событий за последние сутки (24 часа)
     */
    @Override
    public long eventsByLastDay() {
        final long millis = System.currentTimeMillis();
        final long[] hours = new long[86400];
        final long dayHours = millis / MILLIS_IN_DAY;
        synchronized (this.events) {
            if (dayHours <= this.lastDayHours + 1) {
                System.arraycopy(this.events, 0, hours, 0, 86400);
            } else {
                return 0L;
            }
        }
        long sum = 0L;
        for (long events : hours) {
            sum += events;
        }
        return sum;
    }
}
