package com.digitilius.multireestr.calendar;

import java.time.LocalDate;

public interface WorkingCalendar {
    boolean isBusinessDay(LocalDate date);
    DayInfo getDayInfo(LocalDate date);
    LocalDate plus(LocalDate date, int businessDays);
    LocalDate minus(LocalDate date, int businessDays);
}
