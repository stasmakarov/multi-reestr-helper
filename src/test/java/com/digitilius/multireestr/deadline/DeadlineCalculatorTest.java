package com.digitilius.multireestr.deadline;

import com.digitilius.multireestr.calendar.WorkingCalendar;
import com.digitilius.multireestr.entity.CompareOperator;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.ShiftRule;
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
class DeadlineCalculatorTest {

    @Autowired
    DeadlineCalculator deadlineCalculator;

    @Autowired
    WorkingCalendar workingCalendar;

    @Test
    void fridayPlusOneCalendarDayShiftsOffSaturday() {
        DeadlineSpec spec = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY);
        LocalDate friday = LocalDate.of(2025, 4, 4);

        assertThat(deadlineCalculator.deadline(friday, spec)).isEqualTo(LocalDate.of(2025, 4, 7));
    }

    @Test
    void fridayPlusOneCalendarDayStaysOnWorkingTransferSaturday() {
        DeadlineSpec spec = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY);
        LocalDate friday = LocalDate.of(2024, 4, 26);

        assertThat(deadlineCalculator.deadline(friday, spec)).isEqualTo(LocalDate.of(2024, 4, 27));
        assertThat(workingCalendar.isBusinessDay(LocalDate.of(2024, 4, 27))).isTrue();
    }

    @Test
    void newYear2026DueIsFirstSeededBusinessDay() {
        DeadlineSpec spec = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY);
        LocalDate expected = firstBusinessDayOnOrAfter(LocalDate.of(2025, 12, 31));

        assertThat(workingCalendar.isBusinessDay(expected)).isTrue();
        assertThat(deadlineCalculator.deadline(LocalDate.of(2025, 12, 30), spec)).isEqualTo(expected);
        assertThat(deadlineCalculator.nextBusinessDayIfNonWorking(LocalDate.of(2025, 12, 31)))
                .isEqualTo(expected);
    }

    @Test
    void mayHolidaysShiftCalendarDeadline() {
        DeadlineSpec spec = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY);

        assertThat(deadlineCalculator.deadline(LocalDate.of(2024, 4, 30), spec))
                .isEqualTo(LocalDate.of(2024, 5, 2));
        assertThat(deadlineCalculator.deadline(LocalDate.of(2024, 4, 26),
                spec(2, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY)))
                .isEqualTo(LocalDate.of(2024, 5, 2));
    }

    @Test
    void monthPeriodEndsOnSameNumberOrLastDayOfMonth() {
        DeadlineSpec spec = spec(1, DayCountUnit.MONTHS, true, ShiftRule.NONE);

        assertThat(deadlineCalculator.deadline(LocalDate.of(2024, 1, 31), spec))
                .isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(deadlineCalculator.deadline(LocalDate.of(2025, 3, 31), spec))
                .isEqualTo(LocalDate.of(2025, 4, 30));
        assertThat(deadlineCalculator.deadline(LocalDate.of(2024, 1, 31), 1, DayCountUnit.MONTHS))
                .isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void startNextDayFalseCalendarDaysCountEventAsDayOne() {
        DeadlineSpec spec = spec(10, DayCountUnit.CALENDAR_DAYS, false, ShiftRule.NONE);
        LocalDate event = LocalDate.of(2025, 3, 26);

        assertThat(deadlineCalculator.deadline(event, spec)).isEqualTo(event.plusDays(9));
    }

    @Test
    void startNextDayFalseWorkingDaysCountEventIfWorking() {
        DeadlineSpec spec = spec(10, DayCountUnit.WORKING_DAYS, false, ShiftRule.NONE);
        LocalDate wednesday = LocalDate.of(2025, 3, 26);

        assertThat(workingCalendar.isBusinessDay(wednesday)).isTrue();
        assertThat(deadlineCalculator.deadline(wednesday, spec))
                .isEqualTo(workingCalendar.plus(wednesday, 9));
    }

    @Test
    void previousWorkdayShiftsBackFromWeekend() {
        DeadlineSpec spec = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.PREVIOUS_WORKDAY);
        LocalDate friday = LocalDate.of(2025, 4, 4);

        assertThat(deadlineCalculator.deadline(friday, spec)).isEqualTo(friday);
    }

    @Test
    void workingDaysWithNextWorkdayShiftIsNoOp() {
        DeadlineSpec none = spec(10, DayCountUnit.WORKING_DAYS, true, ShiftRule.NONE);
        DeadlineSpec next = spec(10, DayCountUnit.WORKING_DAYS, true, ShiftRule.NEXT_WORKDAY);
        LocalDate lbo = LocalDate.of(2025, 3, 26);

        assertThat(deadlineCalculator.deadline(lbo, next)).isEqualTo(deadlineCalculator.deadline(lbo, none));
    }

    @Test
    void finding1LboTenWorkingDays() {
        DeadlineSpec spec = spec(10, DayCountUnit.WORKING_DAYS, true, ShiftRule.NONE);
        LocalDate lbo = LocalDate.of(2025, 3, 26);
        LocalDate due = LocalDate.of(2025, 4, 9);
        LocalDate approval = LocalDate.of(2025, 4, 10);

        assertThat(deadlineCalculator.deadline(lbo, spec)).isEqualTo(due);
        assertThat(deadlineCalculator.deadline(lbo, 10, DayCountUnit.WORKING_DAYS)).isEqualTo(due);
        assertThat(deadlineCalculator.compare(approval, due, CompareOperator.NOT_LATER)).isFalse();
        assertThat(deadlineCalculator.compare(due, due, CompareOperator.NOT_LATER)).isTrue();
    }

    @Test
    void zeroOffsetIsAnchorThenShift() {
        DeadlineSpec spec = spec(0, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY);
        LocalDate saturday = LocalDate.of(2025, 4, 5);

        assertThat(deadlineCalculator.deadline(saturday, spec)).isEqualTo(LocalDate.of(2025, 4, 7));
        assertThat(deadlineCalculator.deadline(LocalDate.of(2025, 4, 4), spec))
                .isEqualTo(LocalDate.of(2025, 4, 4));
    }

    @Test
    void nullShiftRuleDefaultsByUnit() {
        DeadlineSpec calendar = spec(1, DayCountUnit.CALENDAR_DAYS, true, null);
        DeadlineSpec working = spec(10, DayCountUnit.WORKING_DAYS, true, null);
        LocalDate friday = LocalDate.of(2025, 4, 4);
        LocalDate lbo = LocalDate.of(2025, 3, 26);

        assertThat(deadlineCalculator.deadline(friday, calendar)).isEqualTo(LocalDate.of(2025, 4, 7));
        assertThat(deadlineCalculator.deadline(lbo, working)).isEqualTo(LocalDate.of(2025, 4, 9));
    }

    @Test
    void rejectsNegativeOrMissingRequired() {
        DeadlineSpec missingOffset = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NONE);
        missingOffset.setOffsetValue(null);
        DeadlineSpec missingUnit = spec(1, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NONE);
        missingUnit.setUnit(null);

        assertThatThrownBy(() -> deadlineCalculator.deadline(LocalDate.of(2025, 3, 26), -1, DayCountUnit.CALENDAR_DAYS))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> deadlineCalculator.deadline(LocalDate.of(2025, 3, 26), missingOffset))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> deadlineCalculator.deadline(LocalDate.of(2025, 3, 26), missingUnit))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private LocalDate firstBusinessDayOnOrAfter(LocalDate date) {
        LocalDate current = date;
        while (!workingCalendar.isBusinessDay(current)) {
            current = current.plusDays(1);
        }
        return current;
    }

    private static DeadlineSpec spec(int offset, DayCountUnit unit, Boolean startNextDay, ShiftRule shiftRule) {
        DeadlineSpec spec = new DeadlineSpec();
        spec.setOffsetValue(offset);
        spec.setUnit(unit);
        spec.setStartNextDay(startNextDay);
        spec.setShiftRule(shiftRule);
        return spec;
    }
}
