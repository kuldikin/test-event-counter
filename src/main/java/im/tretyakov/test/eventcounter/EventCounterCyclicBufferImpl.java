package im.tretyakov.test.eventcounter;

/**
 * Реализация интерфейса для учета однотипных событий в системе
 * с использованием кольцевых буферов. Имеет значительную экономию по памяти
 * и оптимизирована для потребителя.
 * <p>
 * Данная реализация обеспечивает регистрацию более 8.000.000 событий в секунду при одном поставщике,
 * но при увеличении количества поставщиков деградирует,
 * хотя даже при числе поставщиков > количества ядер * потоков остаётся более быстрой,
 * чем наивная реализация на Stream API.
 * Может быть оптимизирована для поставщика, увеличив расход памяти с небольшой деградацией для потребителя.
 * <p>
 * Created on 21.02.16.
 *
 * @author tretyakov (dmitry@tretyakov.im)
 */
public class EventCounterCyclicBufferImpl implements EventCounter {

    private static final long MILLIS_IN_SECOND = 1000L;

    private static final long MILLIS_IN_MINUTE = 60 * 1000L;

    private static final long MILLIS_IN_HOUR = 60 * 60 * 1000L;

    private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000L;

    private final long[] seconds = new long[60];

    private final long[] minutes = new long[60];

    private final long[] hours = new long[24];

    private volatile byte secondsPointer = 0;

    private volatile byte minutesPointer = 0;

    private volatile byte hoursPointer = 0;

    private volatile long lastMinuteSeconds = System.currentTimeMillis() / MILLIS_IN_MINUTE;

    private volatile long lastHourMinutes = System.currentTimeMillis() / MILLIS_IN_HOUR;

    private volatile long lastDayHours = System.currentTimeMillis() / MILLIS_IN_DAY;

    /**
     * Учитывает событие
     */
    @Override
    public synchronized void countEvent() {
        final long millis = System.currentTimeMillis();
        final long currentMinuteSeconds = millis / MILLIS_IN_MINUTE;
        final long currentHourMinutes = millis / MILLIS_IN_HOUR;
        final long currentDayHours = millis / MILLIS_IN_DAY;
        final byte secondsIndex = (byte) (millis / MILLIS_IN_SECOND % 60);
        final byte minutesIndex = (byte) (currentMinuteSeconds % 60);
        final byte hoursIndex = (byte) (currentHourMinutes % 24);

        if (currentMinuteSeconds == this.lastMinuteSeconds) {
            if (secondsIndex == secondsPointer) {
                seconds[secondsIndex]++;
            } else {
                if (secondsIndex > secondsPointer) {
                    System.arraycopy(new long[secondsIndex - secondsPointer], 0, this.seconds, secondsPointer + 1, secondsIndex - secondsPointer - 1);
                }
                seconds[secondsIndex] = 1L;
                secondsPointer = secondsIndex;
            }
        } else if (currentMinuteSeconds == this.lastMinuteSeconds + 1) {
            if (secondsIndex == secondsPointer) {
                System.arraycopy(new long[60], 0, this.seconds, 0, 60);
            } else if (secondsIndex < secondsPointer) {
                System.arraycopy(new long[secondsPointer - secondsIndex], 0, this.seconds, secondsIndex, secondsPointer - secondsIndex);
            } else {
                System.arraycopy(new long[60 - secondsIndex], 0, this.seconds, secondsIndex, 60 - secondsIndex);
            }
            seconds[secondsIndex] = 1L;
            secondsPointer = secondsIndex;
        } else {
            System.arraycopy(new long[60], 0, this.seconds, 0, 60);
            seconds[secondsIndex] = 1L;
            secondsPointer = secondsIndex;
        }

        if (currentHourMinutes == this.lastHourMinutes) {
            if (minutesIndex == minutesPointer) {
                minutes[minutesIndex]++;
            } else {
                if (minutesIndex > minutesPointer) {
                    System.arraycopy(new long[minutesIndex - minutesPointer], 0, this.minutes, minutesPointer + 1, minutesIndex - minutesPointer - 1);
                }
                this.minutes[minutesIndex] = 1L;
                this.minutesPointer = minutesIndex;
            }
        } else if (currentHourMinutes == this.lastHourMinutes + 1) {
            if (minutesIndex == this.minutesPointer) {
                System.arraycopy(new long[60], 0, this.minutes, 0, 60);
            } else if (minutesIndex < this.minutesPointer) {
                System.arraycopy(new long[this.minutesPointer - minutesIndex], 0, this.minutes, minutesIndex, this.minutesPointer - minutesIndex);
            } else {
                System.arraycopy(new long[60 - minutesIndex], 0, this.minutes, minutesIndex, 60 - minutesIndex);
            }
            this.minutes[minutesIndex] = 1L;
            this.minutesPointer = minutesIndex;
        } else {
            System.arraycopy(new long[60], 0, this.minutes, 0, 60);
            this.minutes[minutesIndex] = 1L;
            this.minutesPointer = minutesIndex;
        }

        if (currentDayHours == this.lastDayHours) {
            if (hoursIndex == this.hoursPointer) {
                this.hours[hoursIndex]++;
            } else {
                if (hoursIndex > this.hoursPointer) {
                    System.arraycopy(new long[hoursIndex - this.hoursPointer], 0, this.hours, this.hoursPointer + 1, hoursIndex - this.hoursPointer - 1);
                }
                this.hours[hoursIndex] = 1L;
                this.hoursPointer = hoursIndex;
            }
        } else if (currentDayHours == this.lastDayHours + 1) {
            if (hoursIndex == this.hoursPointer) {
                System.arraycopy(new long[24], 0, this.hours, 0, 24);
            } else if (hoursIndex < this.hoursPointer) {
                System.arraycopy(new long[this.hoursPointer - hoursIndex], 0, this.hours, hoursIndex, this.hoursPointer - hoursIndex);
            } else {
                System.arraycopy(new long[24 - hoursIndex], 0, this.hours, hoursIndex, 24 - hoursIndex);
            }
            this.hours[hoursIndex] = 1L;
            this.hoursPointer = hoursIndex;
        } else {
            System.arraycopy(new long[24], 0, this.hours, 0, 24);
            this.hours[hoursIndex] = 1L;
            this.hoursPointer = hoursIndex;
        }


        this.lastMinuteSeconds = currentMinuteSeconds;
        this.lastHourMinutes = currentHourMinutes;
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
        if (millis / MILLIS_IN_MINUTE > this.lastMinuteSeconds + 1) {
            return 0L;
        }
        final byte freshSince;
        synchronized (this) {
            freshSince = (byte) ((millis / MILLIS_IN_SECOND) % 60);
            if (freshSince > secondsPointer) {
                System.arraycopy(this.seconds, freshSince, seconds, 0, 60 - freshSince);
                System.arraycopy(this.seconds, 0, seconds, 60 - freshSince, secondsPointer + 1);
            } else if (freshSince < secondsPointer) {
                System.arraycopy(this.seconds, freshSince, seconds, 0, 61 - secondsPointer);
            } else {
                System.arraycopy(this.seconds, 0, seconds, 0, 60);
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
        final long[] minutes = new long[60];
        if (millis / MILLIS_IN_HOUR > this.lastHourMinutes + 1) {
            return 0L;
        }
        final byte freshSince;
        synchronized (this) {
            freshSince = (byte) ((millis / MILLIS_IN_MINUTE) % 60);
            if (freshSince > minutesPointer) {
                System.arraycopy(this.minutes, freshSince, minutes, 0, 60 - freshSince);
                System.arraycopy(this.minutes, 0, minutes, 60 - freshSince, minutesPointer + 1);
            } else if (freshSince < minutesPointer) {
                System.arraycopy(this.minutes, freshSince, minutes, 0, 61 - minutesPointer);
            } else {
                System.arraycopy(this.minutes, 0, minutes, 0, 60);
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
        final long[] hours = new long[24];
        if (millis / MILLIS_IN_DAY > this.lastDayHours + 1) {
            return 0L;
        }
        final byte freshSince;
        synchronized (this) {
            freshSince = (byte) ((millis / MILLIS_IN_HOUR) % 24);
            if (freshSince > hoursPointer) {
                System.arraycopy(this.hours, freshSince, hours, 0, 24 - freshSince);
                System.arraycopy(this.hours, 0, hours, 24 - freshSince, hoursPointer + 1);
            } else if (freshSince < hoursPointer) {
                System.arraycopy(this.hours, freshSince, hours, 0, 25 - hoursPointer);
            } else {
                System.arraycopy(this.hours, 0, hours, 0, 24);
            }
        }
        long sum = 0L;
        for (long events : hours) {
            sum += events;
        }
        return sum;
    }
}
