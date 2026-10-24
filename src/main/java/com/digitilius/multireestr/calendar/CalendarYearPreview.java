package com.digitilius.multireestr.calendar;

import java.util.List;

public record CalendarYearPreview(
        int year,
        int holidays,
        int weekends,
        int shortened,
        int workingTransfers,
        int existingCount,
        int existingManualCount,
        List<ParsedCalendarDay> days
) {
}
