package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.entity.CalendarSource;
import com.digitilius.multireestr.entity.DayType;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class CalendarYearImportService {

    private final XmlCalendarParser parser;
    private final XmlCalendarClient client;
    private final DataManager dataManager;
    private final DbWorkingCalendar workingCalendar;

    public CalendarYearImportService(
            XmlCalendarParser parser,
            XmlCalendarClient client,
            DataManager dataManager,
            DbWorkingCalendar workingCalendar
    ) {
        this.parser = parser;
        this.client = client;
        this.dataManager = dataManager;
        this.workingCalendar = workingCalendar;
    }

    public CalendarYearPreview previewFromUrl(int year) {
        return preview(year, new ByteArrayInputStream(client.download(year)));
    }

    public CalendarYearPreview preview(int year, InputStream xml) {
        List<ParsedCalendarDay> days = parser.parse(xml, year);
        List<CalendarDay> existing = loadYear(year);
        int manual = 0;
        for (CalendarDay day : existing) {
            if (day.getSource() == CalendarSource.MANUAL) {
                manual++;
            }
        }
        int holidays = 0;
        int weekends = 0;
        int shortened = 0;
        int transfers = 0;
        for (ParsedCalendarDay day : days) {
            DayType type = day.dayType();
            if (type == DayType.HOLIDAY) {
                holidays++;
            } else if (type == DayType.WEEKEND) {
                weekends++;
            } else if (type == DayType.SHORTENED) {
                shortened++;
            } else if (type == DayType.WORKING_TRANSFER) {
                transfers++;
            }
        }
        return new CalendarYearPreview(
                year, holidays, weekends, shortened, transfers,
                existing.size(), manual, List.copyOf(days)
        );
    }

    @Transactional
    public void replaceYear(CalendarYearPreview preview) {
        List<CalendarDay> existing = loadYear(preview.year());
        SaveContext saveContext = new SaveContext();
        for (CalendarDay day : existing) {
            saveContext.removing(day);
        }
        for (ParsedCalendarDay parsed : preview.days()) {
            CalendarDay entity = dataManager.create(CalendarDay.class);
            entity.setDate(parsed.date());
            entity.setYear(parsed.date().getYear());
            entity.setDayType(parsed.dayType());
            entity.setSource(CalendarSource.XMLCALENDAR);
            entity.setMark(blankToNull(parsed.mark()));
            saveContext.saving(entity);
        }
        dataManager.save(saveContext);
        workingCalendar.invalidate(preview.year());
    }

    private List<CalendarDay> loadYear(int year) {
        return dataManager.load(CalendarDay.class)
                .query("select e from mtr_CalendarDay e where e.year = :year")
                .parameter("year", year)
                .list();
    }

    private static String blankToNull(String mark) {
        return mark == null || mark.isBlank() ? null : mark;
    }
}
