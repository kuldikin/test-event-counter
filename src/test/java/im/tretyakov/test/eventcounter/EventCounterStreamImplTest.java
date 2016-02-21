package im.tretyakov.test.eventcounter;

import junit.framework.TestCase;

/**
 * Тесты для наивной реализации интерфейса для учета однотипных событий в системе
 * <p>
 * Created on 21.02.16.
 *
 * @author tretyakov (dmitry@tretyakov.im)
 */
public class EventCounterStreamImplTest extends TestCase {

    public void testCountEvent() throws Exception {
        final EventCounter eventCounter = new EventCounterStreamImpl();
        for (int i = 0; i < 620; i++) {
            Thread.sleep(100L);
            eventCounter.countEvent();
        }
        assertEquals(620, eventCounter.eventsByLastDay());
        assertEquals(620, eventCounter.eventsByLastHour());
        assertEquals(600, eventCounter.eventsByLastMinute(), 10);
    }

    public void testEventsByLastMinute() throws Exception {
        final EventCounter eventCounter = new EventCounterStreamImpl();
        for (int i = 0; i < 620; i++) {
            Thread.sleep(100L);
            eventCounter.countEvent();
        }
        assertEquals(600, eventCounter.eventsByLastMinute(), 10);
    }

    public void testEventsByLastHour() throws Exception {
        final EventCounter eventCounter = new EventCounterStreamImpl();
        for (int i = 0; i < 620; i++) {
            eventCounter.countEvent();
        }
        assertEquals(620, eventCounter.eventsByLastHour());
    }

    public void testEventsByLastDay() throws Exception {
        final EventCounter eventCounter = new EventCounterStreamImpl();
        for (int i = 0; i < 620; i++) {
            eventCounter.countEvent();
        }
        assertEquals(620, eventCounter.eventsByLastDay());
    }

    public void testEventsInMinuteTwoMinutes() throws Exception {
        final EventCounter eventCounter = new EventCounterStreamImpl();
        Thread.sleep(1000L);
        eventCounter.countEvent();
        assertEquals("Через 1 секунду", 1, eventCounter.eventsByLastMinute());
        Thread.sleep(56_000L);
        eventCounter.countEvent();
        eventCounter.countEvent();
        assertEquals("Через 57 секунд", 3, eventCounter.eventsByLastMinute());
        Thread.sleep(5_000L);
        assertEquals("Через 1 минуту и 2 секунды (нет значения старше 60 секунд)", 2, eventCounter.eventsByLastMinute());
        eventCounter.countEvent();
        assertEquals("Через 1 минуту и 2 секунды (нет значения старше 60 секунд, ещё одно событие)", 3, eventCounter.eventsByLastMinute());
    }
}