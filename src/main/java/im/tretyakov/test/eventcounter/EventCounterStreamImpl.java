package im.tretyakov.test.eventcounter;

import com.kuldikin.test.eventcounter.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Наивная реализация интерфейса для учета однотипных событий в системе с использованием потокобезопасной кучи и Stream API.
 * <p>
 * Данная реализация обеспечивает регистрацию более 10.000 событий в секунду даже при количестве поставщиков больше количества возможных
 * одновременных потоков.
 * <p>
 * Из-за того, что при каждой регистрации события мы обходим всю нашу кучу при большом количестве событий за день наблюдается спад
 * производительности. Created on 20.02.16.
 *
 * @author tretyakov (dmitry@tretyakov.im)
 */
public class EventCounterStreamImpl implements EventCounter {

    private static final long MILLIS_IN_SECOND = 1000L;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    private static final int SECONDS_IN_DAY = 24 * SECONDS_IN_HOUR;

    private final Clock clock;

    public EventCounterStreamImpl(Clock clock) {
        this.clock = clock;
    }

    public EventCounterStreamImpl() {
        this(Clock.defaultClock());
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final ConcurrentMap<Long, Long> events = new ConcurrentHashMap<>();

    /**
     * Учитывает событие
     */
    public void countEvent() {
        final long currentSecond = clock.getTime() / MILLIS_IN_SECOND;
        this.events.merge(currentSecond, 1L, (a, b) -> a + b);
        this.events.entrySet().removeIf(entry -> entry.getKey() < currentSecond - SECONDS_IN_DAY);
    }

    /**
     * Выдаёт число событий за последнюю минуту (60 секунд)
     *
     * @return число событий за последнюю минуту (60 секунд)
     */
    public long eventsByLastMinute() {
        return this.events.entrySet().stream().filter(
                entry -> entry.getKey() >= clock.getTime() / MILLIS_IN_SECOND - (SECONDS_IN_MINUTE - 1)
        ).mapToLong(Map.Entry::getValue).reduce(0, (a, b) -> a + b);
    }

    /**
     * Выдаёт число событий за последний час (60 минут)
     *
     * @return число событий за последний час (60 минут)
     */
    public long eventsByLastHour() {
        return this.events.entrySet().stream().filter(
                entry -> entry.getKey() >= clock.getTime() / MILLIS_IN_SECOND - (SECONDS_IN_HOUR - 1)
        ).mapToLong(Map.Entry::getValue).reduce(0, (a, b) -> a + b);
    }

    /**
     * Выдаёт число событий за последние сутки (24 часа)
     *
     * @return число событий за последние сутки (24 часа)
     */
    public long eventsByLastDay() {
        return this.events.entrySet().stream().filter(
                entry -> entry.getKey() >= clock.getTime() / MILLIS_IN_SECOND - (SECONDS_IN_DAY - 1)
        ).mapToLong(Map.Entry::getValue).reduce(0, (a, b) -> a + b);
    }
}
