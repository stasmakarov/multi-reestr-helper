package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.DayType;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public record DayInfo(LocalDate date, boolean businessDay, DayType dayType,
                      @Nullable String mark, boolean fromTable) {
}
