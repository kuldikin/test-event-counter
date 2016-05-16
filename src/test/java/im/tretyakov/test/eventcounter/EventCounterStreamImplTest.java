package im.tretyakov.test.eventcounter;

import com.kuldikin.test.eventcounter.Clock;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;

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
            eventCounter.countEvent();
            Thread.sleep(100L);
        }
        assertEquals(620, eventCounter.eventsByLastDay());
        assertEquals(620, eventCounter.eventsByLastHour());
        assertEquals(585, eventCounter.eventsByLastMinute(), 15);
    }

    public void testEventsByLastMinute() throws Exception {
        final EventCounter eventCounter = new EventCounterStreamImpl();
        for (int i = 0; i < 620; i++) {
            eventCounter.countEvent();
            Thread.sleep(100L);
        }
        assertEquals(585, eventCounter.eventsByLastMinute(), 15);
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

    public void testFullDay() throws Exception {
        Clock.CustomizableClock clock = new Clock.CustomizableClock();
        final EventCounterCyclicBufferImpl eventCounter = new EventCounterCyclicBufferImpl(clock);

        eventCounter.countEvent();
        eventCounter.countEvent();
        assertEquals("Сразуже", 2, eventCounter.eventsByLastMinute());
        assertEquals("Сразуже", 2, eventCounter.eventsByLastHour());
        assertEquals("Сразуже", 2, eventCounter.eventsByLastDay());

        clock.incClock(1000L);
        assertEquals("Через 1 секунду", 2, eventCounter.eventsByLastMinute());
        assertEquals("Через 1 секунду", 2, eventCounter.eventsByLastHour());
        assertEquals("Через 1 секунду", 2, eventCounter.eventsByLastDay());

        clock.incClock(1000L * 60L);
        assertEquals("Через 1 минуту", 0, eventCounter.eventsByLastMinute());
        assertEquals("Через 1 минуту", 2, eventCounter.eventsByLastHour());
        assertEquals("Через 1 минуту", 2, eventCounter.eventsByLastDay());

        eventCounter.countEvent();
        eventCounter.countEvent();
        eventCounter.countEvent();
        assertEquals("Через 1 минуту и добавления ещё трех", 3, eventCounter.eventsByLastMinute());
        assertEquals("Через 1 минуту и добавления ещё трех", 5, eventCounter.eventsByLastHour());
        assertEquals("Через 1 минуту и добавления ещё трех", 5, eventCounter.eventsByLastDay());

        clock.incClock(1000L * 60L * 60L);
        eventCounter.countEvent();
        eventCounter.countEvent();
        eventCounter.countEvent();
        eventCounter.countEvent();
        assertEquals("Через 1 час", 4, eventCounter.eventsByLastMinute());
        assertEquals("Через 1 час", 4, eventCounter.eventsByLastHour());
        assertEquals("Через 1 час", 9, eventCounter.eventsByLastDay());
        
        clock.incClock(1000L * 60L * 60L * 24L);
        eventCounter.countEvent();
        assertEquals("Через 1 день", 1, eventCounter.eventsByLastMinute());
        assertEquals("Через 1 день", 1, eventCounter.eventsByLastHour());
        assertEquals("Через 1 день", 1, eventCounter.eventsByLastDay());

    }
}
