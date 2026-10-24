package com.digitilius.multireestr.deadline;

import com.digitilius.multireestr.calendar.WorkingCalendar;
import com.digitilius.multireestr.entity.CompareOperator;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.ShiftRule;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@RequireDocker
class DeadlineCalculatorLegacyTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    DeadlineCalculator deadlineCalculator;

    @Autowired
    NoticeCardService noticeCardService;

    @Autowired
    WorkingCalendar workingCalendar;

    @Test
    void finding6NoticeChangeSevenCalendarDays() {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse("0173100017026000008"))
                .orElseThrow();
        LocalDate changePublished = notice.getPublishedAt().toLocalDate();
        LocalDate applicationsEnd = notice.getApplicationsTo().toLocalDate();
        LocalDate expectedDue = LocalDate.of(2026, 3, 5);

        DeadlineSpec spec = spec(7, DayCountUnit.CALENDAR_DAYS, true, null);

        assertThat(changePublished).isEqualTo(LocalDate.of(2026, 2, 26));
        assertThat(applicationsEnd).isEqualTo(LocalDate.of(2026, 3, 3));
        assertThat(notice.getModifVersion()).isGreaterThan(1);

        LocalDate due = deadlineCalculator.deadline(changePublished, spec);
        assertThat(due).isEqualTo(expectedDue);
        assertThat(workingCalendar.isBusinessDay(expectedDue)).isTrue();
        assertThat(deadlineCalculator.compare(applicationsEnd, due, CompareOperator.NOT_EARLIER)).isFalse();
        assertThat(deadlineCalculator.compare(expectedDue, due, CompareOperator.NOT_EARLIER)).isTrue();
    }

    @Test
    void finding9ContractSignedThirtyCalendarDays() {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse("0173100017025000020"))
                .orElseThrow();
        ContractDto contract = notice.getContract();
        ExecutionDto acceptance = contract.getExecutions().stream()
                .filter(execution -> execution.getKind() == ExecutionKind.ACCEPTANCE)
                .findFirst()
                .orElseThrow();
        LocalDate signed = contract.getDate();
        LocalDate deliveryDocument = acceptance.getDocDate();
        LocalDate expectedDue = LocalDate.of(2026, 1, 22);

        DeadlineSpec spec = spec(30, DayCountUnit.CALENDAR_DAYS, true, ShiftRule.NEXT_WORKDAY);

        assertThat(contract.getId()).isEqualTo(18);
        assertThat(signed).isEqualTo(LocalDate.of(2025, 12, 23));
        assertThat(deliveryDocument).isEqualTo(LocalDate.of(2026, 1, 23));

        LocalDate due = deadlineCalculator.deadline(signed, spec);
        assertThat(due).isEqualTo(expectedDue);
        assertThat(deadlineCalculator.compare(deliveryDocument, due, CompareOperator.NOT_LATER)).isFalse();
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
