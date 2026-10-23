package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.CalendarDay;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CalendarDayChangedListener {

    private final DbWorkingCalendar workingCalendar;

    public CalendarDayChangedListener(DbWorkingCalendar workingCalendar) {
        this.workingCalendar = workingCalendar;
    }

    @EventListener
    public void onCalendarDayChanged(EntityChangedEvent<CalendarDay> event) {
        workingCalendar.invalidate();
    }
}
