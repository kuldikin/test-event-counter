package im.tretyakov.test.eventcounter;

/**
 * Реализовать объект для учета однотипных событий в системе.
 * Например, отправка фото в сервисе фотографий.
 * <p>
 * События поступают в произвольный момент времени. Возможно как 10К событий в секунду так и 2 в час.
 * <p>
 * Интерфейс:
 * 1. Учесть событие.
 * 2. Выдать число событий за последнюю минуту (60 секунд).
 * 3. Выдать число событий за последний час (60 минут).
 * 4. Выдать число событий за последние сутки (24 часа).
 * <p>
 * Created on 20.02.16.
 *
 * @author tretyakov (dmitry@tretyakov.im)
 */
public class Main {

    public static void main(String... args) throws Exception {
        final EventCounter eventCounterCyclic = new EventCounterCyclicBufferImpl();
        final long eventsNumber = Long.valueOf(args[0]);
        long start = System.currentTimeMillis();
        {
            for (long i = 0L; i < eventsNumber; i++) {
                eventCounterCyclic.countEvent();
            }
            System.out.println("[Cyclic] Events by last minute: " + eventCounterCyclic.eventsByLastMinute());
            System.out.println("[Cyclic] Events by last hour: " + eventCounterCyclic.eventsByLastHour());
            System.out.println("[Cyclic] Events by last day: " + eventCounterCyclic.eventsByLastDay());
            System.out.printf("[Cyclic] Elapsed time: %d seconds", (System.currentTimeMillis() - start) / 1000L);
        }
        {
            start = System.currentTimeMillis();
            final EventCounter eventCounterSimple = new EventCounterStreamImpl();
            for (long i = 0L; i < eventsNumber; i++) {
                eventCounterSimple.countEvent();
            }
            System.out.println("[Simple] Events by last minute: " + eventCounterSimple.eventsByLastMinute());
            System.out.println("[Simple] Events by last hour: " + eventCounterSimple.eventsByLastHour());
            System.out.println("[Simple] Events by last day: " + eventCounterSimple.eventsByLastDay());
            System.out.printf("[Simple] Elapsed time: %d seconds", (System.currentTimeMillis() - start) / 1000L);
        }
    }
}
