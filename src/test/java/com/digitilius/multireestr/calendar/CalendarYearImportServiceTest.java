package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.CalendarDay;
import com.digitilius.multireestr.entity.CalendarSource;
import com.digitilius.multireestr.entity.DayType;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class CalendarYearImportServiceTest {

    @Autowired
    CalendarYearImportService importService;
    @Autowired
    DataManager dataManager;
    @Autowired
    WorkingCalendar workingCalendar;

    @Test
    void importThenReimportReplacesYearIncludingManual() throws Exception {
        LocalDate manualDate = LocalDate.of(2024, 6, 1);
        dataManager.load(CalendarDay.class)
                .query("select e from mtr_CalendarDay e where e.date = :date")
                .parameter("date", manualDate)
                .optional()
                .ifPresent(dataManager::remove);

        CalendarDay manual = dataManager.create(CalendarDay.class);
        manual.setDate(manualDate);
        manual.setYear(2024);
        manual.setDayType(DayType.WORKING);
        manual.setSource(CalendarSource.MANUAL);
        manual.setMark("ручная правка");
        dataManager.save(manual);
        assertThat(workingCalendar.getDayInfo(LocalDate.of(2024, 6, 1)).fromTable()).isTrue();
        assertThat(workingCalendar.isBusinessDay(LocalDate.of(2024, 6, 1))).isTrue();

        CalendarYearPreview first;
        try (InputStream xml = resource()) {
            first = importService.preview(2024, xml);
        }
        assertThat(first.existingManualCount()).isGreaterThanOrEqualTo(1);
        assertThat(first.workingTransfers()).isEqualTo(2);
        importService.replaceYear(first);

        List<CalendarDay> afterFirst = loadYear(2024);
        assertThat(afterFirst).hasSize(first.days().size());
        assertThat(afterFirst).allMatch(day -> day.getSource() == CalendarSource.XMLCALENDAR);
        assertThat(afterFirst).noneMatch(day -> LocalDate.of(2024, 6, 1).equals(day.getDate()));
        assertThat(workingCalendar.getDayInfo(LocalDate.of(2024, 6, 1)).fromTable()).isFalse();
        assertThat(workingCalendar.isBusinessDay(LocalDate.of(2024, 6, 1))).isFalse();

        CalendarYearPreview second;
        try (InputStream xml = resource()) {
            second = importService.preview(2024, xml);
        }
        assertThat(second.existingCount()).isEqualTo(first.days().size());
        assertThat(second.existingManualCount()).isZero();
        importService.replaceYear(second);

        List<CalendarDay> afterSecond = loadYear(2024);
        assertThat(afterSecond).hasSize(afterFirst.size());
        assertThat(afterSecond).allMatch(day -> day.getSource() == CalendarSource.XMLCALENDAR);
        assertThat(afterSecond)
                .extracting(CalendarDay::getDate)
                .containsExactlyInAnyOrderElementsOf(
                        afterFirst.stream().map(CalendarDay::getDate).toList()
                );
    }

    private List<CalendarDay> loadYear(int year) {
        return dataManager.load(CalendarDay.class)
                .query("select e from mtr_CalendarDay e where e.year = :year")
                .parameter("year", year)
                .list();
    }

    private static InputStream resource() {
        InputStream xml = CalendarYearImportServiceTest.class.getResourceAsStream("/calendar/xmlcalendar-2024.xml");
        assertThat(xml).isNotNull();
        return xml;
    }
}
