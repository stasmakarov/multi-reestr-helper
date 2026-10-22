package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.entity.DayType;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DbWorkingCalendar implements WorkingCalendar {

    private final DataManager dataManager;
    private final ConcurrentHashMap<Integer, Map<LocalDate, CalendarDay>> cache = new ConcurrentHashMap<>();

    public DbWorkingCalendar(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean isBusinessDay(LocalDate date) {
        return getDayInfo(date).businessDay();
    }

    @Override
    public DayInfo getDayInfo(LocalDate date) {
        CalendarDay row = yearMap(date.getYear()).get(date);
        if (row != null) {
            DayType dayType = row.getDayType();
            return new DayInfo(date, dayType.isBusiness(), dayType, row.getMark(), true);
        }
        boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        DayType dayType = weekend ? DayType.WEEKEND : DayType.WORKING;
        return new DayInfo(date, !weekend, dayType, null, false);
    }

    @Override
    public LocalDate plus(LocalDate date, int businessDays) {
        if (businessDays < 0) {
            return minus(date, -businessDays);
        }
        return shift(date, businessDays, 1);
    }

    @Override
    public LocalDate minus(LocalDate date, int businessDays) {
        if (businessDays < 0) {
            return plus(date, -businessDays);
        }
        return shift(date, businessDays, -1);
    }

    public void invalidate() {
        cache.clear();
    }

    public void invalidate(int year) {
        cache.remove(year);
    }

    private LocalDate shift(LocalDate date, int businessDays, int step) {
        LocalDate current = date;
        int remaining = businessDays;
        int guard = 0;
        int maxSteps = Math.max(366, businessDays * 3 + 14);
        while (remaining > 0) {
            current = current.plusDays(step);
            if (isBusinessDay(current)) {
                remaining--;
            }
            if (++guard > maxSteps) {
                throw new IllegalStateException("Не удалось отсчитать " + businessDays
                        + " рабочих дней от " + date);
            }
        }
        return current;
    }

    private Map<LocalDate, CalendarDay> yearMap(int year) {
        return cache.computeIfAbsent(year, this::loadYear);
    }

    private Map<LocalDate, CalendarDay> loadYear(int year) {
        List<CalendarDay> days = dataManager.load(CalendarDay.class)
                .query("select e from mtr_CalendarDay e where e.year = :year")
                .parameter("year", year)
                .list();
        Map<LocalDate, CalendarDay> map = new HashMap<>();
        for (CalendarDay day : days) {
            map.put(day.getDate(), day);
        }
        return map;
    }
}
