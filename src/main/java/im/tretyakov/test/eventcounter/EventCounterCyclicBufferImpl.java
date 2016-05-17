package im.tretyakov.test.eventcounter;

import com.kuldikin.test.eventcounter.Clock;

/**
 * Реализация интерфейса для учета однотипных событий в системе с использованием кольцевых буферов.
 * <p>
 * Данная реализация обеспечивает регистрацию более 8.000.000 событий в секунду при одном поставщике.
 * <p>
 * Реализация основана накольцевом буфере размера 86400 элементов (количество секунд в сутках). 
 * Буфер содержит пару значений: номер секунды в сутках(индекс времени) и колличество событий.
 * Первая секунда суток лежит в 0 элементе буфера, последняя в последнем.
 * <p>
 * При добавлении события мы заботимся об удалении событий старше 24 часов из буфера.
 * Также после добавления события мы сохраняем номер суток и индекс времени последнего изменения, это 
 * позволяет более оптимально производить чистку старых событий при добавлении следующего элемента.
 * <p>
 * Эта реализация более устойчива к большому количеству нагрузок за счёт оптимизированой чистки.
 * 
 * Created on 21.02.16.
 *
 * @author tretyakov (dmitry@tretyakov.im)
 */
public class EventCounterCyclicBufferImpl implements EventCounter {

    private static final long MILLIS_IN_SECOND = 1000L;

    private static final int SECONDS_IN_DAY = 24 * 60 * 60;

    private static final long MILLIS_IN_DAY = SECONDS_IN_DAY * 1000L;

    private final long[] events = new long[SECONDS_IN_DAY];

    private volatile int eventsPointer = 0;

    private volatile long lastDayHours;

    private final Clock clock;

    public EventCounterCyclicBufferImpl(Clock clock) {
        this.clock = clock;
        this.lastDayHours = clock.getTime() / MILLIS_IN_DAY;
    }

    public EventCounterCyclicBufferImpl() {
        this(Clock.defaultClock());
    }

    /**
     * Учитывает событие
     */
    @Override
    public synchronized void countEvent() {
        final long millis = clock.getTime();
        final long currentDayHours = millis / MILLIS_IN_DAY;//кол-во дней прош. от 1970
        final int currentIndex = (int) (millis / MILLIS_IN_SECOND) % SECONDS_IN_DAY;//секунда в дне 0-86399 (Индекс времени)
        if (currentDayHours == this.lastDayHours) {
            //если пред. событие было в этот день
            if (currentIndex == this.eventsPointer) {
                //если пред. событие было в эту же сек.
                this.events[currentIndex]++;
            } else {
                if (currentIndex < this.eventsPointer) {
                    //если 
                    System.arraycopy(
                            new long[this.eventsPointer - currentIndex],
                            0,
                            this.events,
                            currentIndex,
                            this.eventsPointer - currentIndex
                    );//чистим часть от currentIndex до eventsPointer
                }
                this.events[currentIndex] = 1L;
                this.eventsPointer = currentIndex;
            }
        } else if (currentDayHours == this.lastDayHours + 1) {
            //если пред. событие было вчера
            if (currentIndex == this.eventsPointer) {
                //вчера с такимже индексом времени (в ту же сек.)
                System.arraycopy(new long[SECONDS_IN_DAY], 0, this.events, 0, SECONDS_IN_DAY);//чистим всю кучу
            } else if (currentIndex < this.eventsPointer) {
                //вчера но не более чем 24 часа
                System.arraycopy(
                        new long[this.eventsPointer - currentIndex],
                        0,
                        this.events,
                        currentIndex,
                        this.eventsPointer - currentIndex
                );//чистим часть от currentIndex до eventsPointer//чистим часть от currentIndex до eventsPointer
            } else {
                System.arraycopy(new long[SECONDS_IN_DAY - currentIndex], 0, this.events, currentIndex, SECONDS_IN_DAY - currentIndex);
                //чистим всё после currentIndex
            }
            this.events[currentIndex] = 1L;
            this.eventsPointer = currentIndex;
        } else {
            //если пред. событие было больше чм 24 часа назад
            System.arraycopy(new long[SECONDS_IN_DAY], 0, this.events, 0, SECONDS_IN_DAY);
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
        final long millis = clock.getTime();
        final long[] seconds = new long[60];
        final long dayHours = millis / MILLIS_IN_DAY;//кол-во дней прош. от 1970
        final int currentPointer = (int) ((millis / MILLIS_IN_SECOND) % SECONDS_IN_DAY);//секунда в дне 0-86399 (Индекс времени)
        synchronized (this.events) {
            if (dayHours <= this.lastDayHours + 1) {
                //если последнее событие было вчера(после 23:59) или сегодня
                if (currentPointer > 58) {
                    //если индекс времени сейчас меньше минуты то копируем последнии 60 секунд
                    System.arraycopy(this.events, currentPointer - 59, seconds, 0, 60);
                } else {
                    //а если иначе, то придется захватить кусок с конца и кусок с начала буфера
                    System.arraycopy(this.events, (SECONDS_IN_DAY - 1) - (58 - currentPointer), seconds, 0, 59 - currentPointer);
                    System.arraycopy(this.events, 0, seconds, 59 - currentPointer, currentPointer + 1);
                }
            } else {
                return 0L;
            }
        }
        //считаем сумму уже по копии массива, что бы не блокировать на долго
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
        final long millis = clock.getTime();
        final long[] minutes = new long[3600];
        final long dayHours = millis / MILLIS_IN_DAY;//кол-во дней прош. от 1970
        final int currentPointer = (int) ((millis / MILLIS_IN_SECOND) % SECONDS_IN_DAY);//секунда в дне 0-86399 (Индекс времени)
        synchronized (this.events) {
            if (dayHours <= this.lastDayHours + 1) {
                //если последнее событие было вчера(после 23:00) или сегодня
                if (currentPointer > 3598) {
                    //если индекс времени сейчас меньше часа то копируем следующий час
                    System.arraycopy(this.events, currentPointer - 3599, minutes, 0, 3600);
                } else {
                    //а если иначе, то придется захватить кусок с конца и кусок с начала буфера
                    System.arraycopy(this.events, (SECONDS_IN_DAY - 1) - (3598 - currentPointer), minutes, 0, 3599 - currentPointer);
                    System.arraycopy(this.events, 0, minutes, 3599 - currentPointer, currentPointer + 1);
                }
            } else {
                return 0L;
            }
        }
        //считаем сумму уже по копии массива, что бы не блокировать на долго
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
        final long millis = clock.getTime();
        final long[] hours = new long[SECONDS_IN_DAY];
        final long dayHours = millis / MILLIS_IN_DAY;//кол-во дней прош. от 1970
        synchronized (this.events) {
            if (dayHours <= this.lastDayHours + 1) {
                //если последний день в котором было событие = вчера или сегодня
                System.arraycopy(this.events, 0, hours, 0, SECONDS_IN_DAY);
            } else {
                return 0L;
            }
        }
        //считаем сумму уже по копии массива, что бы не блокировать на долго 
        long sum = 0L;
        for (long events : hours) {
            sum += events;
        }
        return sum;
    }
}
