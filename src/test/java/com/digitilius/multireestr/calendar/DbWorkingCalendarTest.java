package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.DayType;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class DbWorkingCalendarTest {

    @Autowired
    WorkingCalendar workingCalendar;

    @Test
    void tableOverridesDefaultWeekendAndHolidayRules() {
        LocalDate transferredSaturday = LocalDate.of(2024, 4, 27);
        LocalDate holidayWednesday = LocalDate.of(2024, 5, 1);
        LocalDate shortenedWednesday = LocalDate.of(2024, 5, 8);
        LocalDate ordinarySunday = LocalDate.of(2024, 4, 21);
        LocalDate ordinaryThursday = LocalDate.of(2024, 4, 25);

        DayInfo saturday = workingCalendar.getDayInfo(transferredSaturday);
        assertThat(saturday.fromTable()).isTrue();
        assertThat(saturday.dayType()).isEqualTo(DayType.WORKING_TRANSFER);
        assertThat(saturday.businessDay()).isTrue();
        assertThat(workingCalendar.isBusinessDay(transferredSaturday)).isTrue();

        DayInfo holiday = workingCalendar.getDayInfo(holidayWednesday);
        assertThat(holiday.fromTable()).isTrue();
        assertThat(holiday.dayType()).isEqualTo(DayType.HOLIDAY);
        assertThat(holiday.businessDay()).isFalse();

        DayInfo shortened = workingCalendar.getDayInfo(shortenedWednesday);
        assertThat(shortened.fromTable()).isTrue();
        assertThat(shortened.dayType()).isEqualTo(DayType.SHORTENED);
        assertThat(shortened.businessDay()).isTrue();

        DayInfo sunday = workingCalendar.getDayInfo(ordinarySunday);
        assertThat(sunday.fromTable()).isFalse();
        assertThat(sunday.dayType()).isEqualTo(DayType.WEEKEND);
        assertThat(sunday.businessDay()).isFalse();

        DayInfo thursday = workingCalendar.getDayInfo(ordinaryThursday);
        assertThat(thursday.fromTable()).isFalse();
        assertThat(thursday.dayType()).isEqualTo(DayType.WORKING);
        assertThat(thursday.businessDay()).isTrue();
    }

    @Test
    void plusAndMinusSkipHolidaysAndCountTransfers() {
        LocalDate friday = LocalDate.of(2024, 4, 26);
        assertThat(workingCalendar.plus(friday, 1)).isEqualTo(LocalDate.of(2024, 4, 27));
        assertThat(workingCalendar.plus(friday, 2)).isEqualTo(LocalDate.of(2024, 5, 2));
        assertThat(workingCalendar.minus(LocalDate.of(2024, 5, 2), 2)).isEqualTo(friday);
        assertThat(workingCalendar.plus(LocalDate.of(2024, 5, 8), 1)).isEqualTo(LocalDate.of(2024, 5, 13));
        assertThat(workingCalendar.plus(friday, 0)).isEqualTo(friday);
    }

    @Test
    void missingYearDoesNotFallBackToWeekdays() {
        LocalDate unloaded = LocalDate.of(2099, 1, 6);
        assertThatThrownBy(() -> workingCalendar.isBusinessDay(unloaded))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2099")
                .hasMessageContaining("не загружен в производственный календарь");
    }
}
