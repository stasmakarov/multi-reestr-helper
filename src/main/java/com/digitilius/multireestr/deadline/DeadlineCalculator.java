package com.digitilius.multireestr.deadline;

import com.digitilius.multireestr.calendar.WorkingCalendar;
import com.digitilius.multireestr.entity.CompareOperator;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.ShiftRule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Расчёт срока по ст. 191–193 ГК РФ поверх {@link WorkingCalendar}.
 * Сам календарь отвечает только на вопрос «рабочий ли день».
 *
 * <p>Если {@link DeadlineSpec#getShiftRule()} не задан, перенос берётся по единице срока
 * (см. {@code ShiftRule.md}): для {@link DayCountUnit#CALENDAR_DAYS} и
 * {@link DayCountUnit#MONTHS} — {@link ShiftRule#NEXT_WORKDAY} (ст. 193 ГК);
 * для {@link DayCountUnit#WORKING_DAYS} — {@link ShiftRule#NONE}.
 */
@Component
public class DeadlineCalculator {

    private final WorkingCalendar workingCalendar;

    public DeadlineCalculator(WorkingCalendar workingCalendar) {
        this.workingCalendar = workingCalendar;
    }

    /**
     * Срок в N единицах со следующего дня после события (ст. 191), без переноса окончания.
     */
    public LocalDate deadline(LocalDate event, int n, DayCountUnit unit) {
        Objects.requireNonNull(event, "event");
        requireUnit(unit);
        requireNonNegative(n);
        return addOffset(event, n, unit, true);
    }

    /**
     * Срок по формуле: смещение, единица, {@code startNextDay} (по умолчанию true) и перенос.
     *
     * <p>{@code N = 0} — дата якоря, затем перенос. Отрицательное или пустое смещение,
     * пустая единица — {@link IllegalArgumentException}.
     */
    public LocalDate deadline(LocalDate anchorDate, DeadlineSpec spec) {
        Objects.requireNonNull(anchorDate, "anchorDate");
        Objects.requireNonNull(spec, "spec");
        DayCountUnit unit = spec.getUnit();
        requireUnit(unit);
        Integer offset = spec.getOffsetValue();
        if (offset == null) {
            throw new IllegalArgumentException("offsetValue");
        }
        requireNonNegative(offset);
        boolean startNextDay = spec.getStartNextDay() == null || spec.getStartNextDay();
        LocalDate raw = addOffset(anchorDate, offset, unit, startNextDay);
        return applyShift(raw, resolveShiftRule(spec.getShiftRule(), unit));
    }

    public LocalDate nextBusinessDayIfNonWorking(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return applyShift(date, ShiftRule.NEXT_WORKDAY);
    }

    /**
     * {@link CompareOperator#NOT_LATER}: {@code target ≤ due};
     * {@link CompareOperator#NOT_EARLIER}: {@code target ≥ due}.
     * Равенство — успех.
     */
    public boolean compare(LocalDate targetDate, LocalDate dueDate, CompareOperator compareOperator) {
        Objects.requireNonNull(targetDate, "targetDate");
        Objects.requireNonNull(dueDate, "dueDate");
        Objects.requireNonNull(compareOperator, "compareOperator");
        return switch (compareOperator) {
            case NOT_LATER -> !targetDate.isAfter(dueDate);
            case NOT_EARLIER -> !targetDate.isBefore(dueDate);
        };
    }

    private LocalDate addOffset(LocalDate anchor, int n, DayCountUnit unit, boolean startNextDay) {
        if (n == 0) {
            return anchor;
        }
        return switch (unit) {
            case CALENDAR_DAYS -> startNextDay
                    ? anchor.plusDays(n)
                    : anchor.plusDays(n - 1L);
            case WORKING_DAYS -> addWorkingDays(anchor, n, startNextDay);
            case MONTHS -> startNextDay
                    ? anchor.plusMonths(n)
                    : anchor.plusMonths(n).minusDays(1);
        };
    }

    private LocalDate addWorkingDays(LocalDate anchor, int n, boolean startNextDay) {
        if (startNextDay) {
            return workingCalendar.plus(anchor, n);
        }
        if (workingCalendar.isBusinessDay(anchor)) {
            return workingCalendar.plus(anchor, n - 1);
        }
        return workingCalendar.plus(anchor, n);
    }

    /**
     * Пустой {@code shiftRule}: календарные дни и месяцы — {@link ShiftRule#NEXT_WORKDAY};
     * рабочие дни — {@link ShiftRule#NONE}.
     */
        private ShiftRule resolveShiftRule(ShiftRule shiftRule, DayCountUnit unit) {
        if (shiftRule != null) {
            return shiftRule;
        }
        return unit == DayCountUnit.WORKING_DAYS ? ShiftRule.NONE : ShiftRule.NEXT_WORKDAY;
    }

    private LocalDate applyShift(LocalDate date, ShiftRule shiftRule) {
        if (shiftRule == ShiftRule.NONE || workingCalendar.isBusinessDay(date)) {
            return date;
        }
        int step = shiftRule == ShiftRule.PREVIOUS_WORKDAY ? -1 : 1;
        LocalDate current = date;
        int guard = 0;
        while (!workingCalendar.isBusinessDay(current)) {
            current = current.plusDays(step);
            if (++guard > 366) {
                throw new IllegalStateException("Не удалось перенести срок с " + date);
            }
        }
        return current;
    }

    private static void requireUnit(DayCountUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("unit");
        }
    }

    private static void requireNonNegative(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("offsetValue");
        }
    }
}
