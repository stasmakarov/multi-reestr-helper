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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class CalendarLiquibaseCoverageTest {

    @Autowired
    DataManager dataManager;

    @Test
    void seedCoversFixtureYears() {
        List<CalendarDay> days = dataManager.load(CalendarDay.class)
                .query("select e from mtr_CalendarDay e")
                .list();
        Set<Integer> xmlYears = days.stream()
                .filter(day -> day.getSource() == CalendarSource.XMLCALENDAR)
                .map(CalendarDay::getYear)
                .collect(Collectors.toSet());
        assertThat(xmlYears).contains(2024, 2025, 2026, 2027);

        CalendarDay transfer = dataManager.load(CalendarDay.class)
                .query("select e from mtr_CalendarDay e where e.date = :date")
                .parameter("date", LocalDate.of(2024, 4, 27))
                .one();
        assertThat(transfer.getDayType()).isEqualTo(DayType.WORKING_TRANSFER);
        assertThat(transfer.getSource()).isEqualTo(CalendarSource.XMLCALENDAR);
        assertThat(transfer.getYear()).isEqualTo(2024);
    }
}
