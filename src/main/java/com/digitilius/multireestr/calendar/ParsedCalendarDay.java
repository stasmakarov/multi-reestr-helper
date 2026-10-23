package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.DayType;

import java.time.LocalDate;

public record ParsedCalendarDay(LocalDate date, DayType dayType, String mark) {
}
