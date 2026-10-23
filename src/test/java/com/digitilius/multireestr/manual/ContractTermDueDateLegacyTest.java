package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.calendar.WorkingCalendar;
import com.digitilius.multireestr.deadline.DeadlineCalculator;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.ShiftRule;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
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
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class ContractTermDueDateLegacyTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ContractTermDueDateService dueDateService;
    @Autowired
    DeadlineCalculator deadlineCalculator;
    @Autowired
    WorkingCalendar workingCalendar;
    @Autowired
    NoticeCardService noticeCardService;
    @Autowired
    DataManager dataManager;

    @Test
    void notice3DeliveryThirtyCalendarDaysUsesCalendar() {
        NoticeDto notice = notice(3, "0173100017026000015");
        ContractDto contract = notice.getContract();
        assertThat(contract.getId()).isEqualTo(30);

        DeadlineSpec spec = formula(DeadlineAnchor.CONTRACT_SIGNED, 30, DayCountUnit.CALENDAR_DAYS, ShiftRule.NEXT_WORKDAY);
        ContractTerm term = persistTerm(contract.getId(), notice.getNumber(), spec, null);
        try {
            LocalDate due = dueDateService.calculate(term).orElseThrow();
            LocalDate expected = deadlineCalculator.deadline(contract.getDate(), spec);
            assertThat(due).isEqualTo(expected);
            assertThat(workingCalendar.isBusinessDay(due)).isTrue();
        } finally {
            dataManager.remove(term);
        }
    }

    @Test
    void notice17SignedPlusTenWorkingDays() {
        NoticeDto notice = notice(17, "0173100017026000001");
        ContractDto contract = notice.getContract();
        assertThat(contract.getId()).isEqualTo(19);

        DeadlineSpec spec = formula(DeadlineAnchor.CONTRACT_SIGNED, 10, DayCountUnit.WORKING_DAYS, ShiftRule.NONE);
        ContractTerm term = persistTerm(contract.getId(), notice.getNumber(), spec, null);
        try {
            LocalDate due = dueDateService.calculate(term).orElseThrow();
            assertThat(due).isEqualTo(deadlineCalculator.deadline(contract.getDate(), spec));
            assertThat(workingCalendar.isBusinessDay(due)).isTrue();
        } finally {
            dataManager.remove(term);
        }
    }

    @Test
    void notice38HasNoContractSoSignedAnchorIsEmpty() {
        NoticeDto notice = notice(38, "0173100017024000010");
        assertThat(notice.getContract()).isNull();

        DeadlineSpec spec = formula(DeadlineAnchor.CONTRACT_SIGNED, 30, DayCountUnit.CALENDAR_DAYS, ShiftRule.NEXT_WORKDAY);
        ContractTerm term = dataManager.create(ContractTerm.class);
        term.setContractLegacyId(999001);
        term.setPurchaseNumber(notice.getNumber());
        term.setTermType(ContractTermType.DELIVERY);
        term.setTerm(spec);

        assertThat(dueDateService.calculate(term)).isEmpty();
    }

    @Test
    void notice40LboTenWorkingDaysFinding1() {
        NoticeDto notice = notice(40, "0173100017024000008");
        ContractDto contract = notice.getContract();
        assertThat(contract.getId()).isEqualTo(3);

        PlanScheduleEvent plan = dataManager.create(PlanScheduleEvent.class);
        plan.setFinYear(2024);
        plan.setPlanVersion(99);
        plan.setLboDate(LocalDate.of(2025, 3, 26));
        plan.setApprovalDate(LocalDate.of(2025, 4, 10));
        plan = dataManager.save(plan);

        DeadlineSpec spec = formula(DeadlineAnchor.LBO_DATE, 10, DayCountUnit.WORKING_DAYS, ShiftRule.NONE);
        ContractTerm term = persistTerm(contract.getId(), notice.getNumber(), spec, null);
        try {
            LocalDate due = dueDateService.calculate(term).orElseThrow();
            assertThat(due).isEqualTo(LocalDate.of(2025, 4, 9));
            assertThat(workingCalendar.isBusinessDay(due)).isTrue();
            assertThat(deadlineCalculator.compare(plan.getApprovalDate(), due,
                    com.digitilius.multireestr.entity.CompareOperator.NOT_LATER)).isFalse();
        } finally {
            dataManager.remove(term);
            dataManager.remove(plan);
        }
    }

    @Test
    void fixedDateIsUsedInsteadOfFormula() {
        NoticeDto notice = notice(3, "0173100017026000015");
        ContractTerm term = persistTerm(notice.getContract().getId(), notice.getNumber(), null,
                LocalDate.of(2026, 5, 1));
        try {
            assertThat(dueDateService.calculate(term)).contains(LocalDate.of(2026, 5, 1));
        } finally {
            dataManager.remove(term);
        }
    }

    private NoticeDto notice(int expectedId, String number) {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse(number)).orElseThrow();
        assertThat(notice.getId()).isEqualTo(expectedId);
        return notice;
    }

    private ContractTerm persistTerm(Integer contractId, String purchaseNumber, DeadlineSpec spec, LocalDate fixed) {
        ContractTerm term = dataManager.create(ContractTerm.class);
        term.setContractLegacyId(contractId);
        term.setPurchaseNumber(purchaseNumber);
        term.setTermType(ContractTermType.DELIVERY);
        term.setTerm(spec);
        term.setFixedDate(fixed);
        return dataManager.save(term);
    }

    private static DeadlineSpec formula(DeadlineAnchor anchor, int offset, DayCountUnit unit, ShiftRule shiftRule) {
        DeadlineSpec spec = new DeadlineSpec();
        spec.setAnchor(anchor);
        spec.setOffsetValue(offset);
        spec.setUnit(unit);
        spec.setStartNextDay(true);
        spec.setShiftRule(shiftRule);
        return spec;
    }
}
