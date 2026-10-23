package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.ShiftRule;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.jmix.data.exception.UniqueConstraintViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@RequireDocker
class ContractTermDueDateServiceTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ContractTermDueDateService contractTermDueDateService;
    @Autowired
    NoticeCardService noticeCardService;
    @Autowired
    DataManager dataManager;

    @Test
    void contractSignedUsesCalendarDayForGoldenNotice3() {
        NoticeDto notice = noticeCardService.loadNotice(3).orElseThrow();
        ContractTerm term = formulaTerm(30, notice.getContract().getId(), notice.getNumber());

        assertThat(notice.getContract().getDate()).isEqualTo(LocalDate.of(2026, 4, 27));
        assertThat(contractTermDueDateService.dueDate(term, notice, notice.getContract(), null))
                .contains(LocalDate.of(2026, 5, 27));
    }

    @Test
    void notice38WithoutContractHasEmptyDueDate() {
        NoticeDto notice = noticeCardService.loadNotice(38).orElseThrow();
        ContractTerm term = formulaTerm(30, 38, notice.getNumber());

        assertThat(notice.getContract()).isNull();
        assertThat(contractTermDueDateService.dueDate(term, notice, null, null)).isEmpty();
    }

    @Test
    void fixedDateWinsOverFormula() {
        NoticeDto notice = noticeCardService.loadNotice(17).orElseThrow();
        ContractTerm term = formulaTerm(30, notice.getContract().getId(), notice.getNumber());
        term.setFixedDate(LocalDate.of(2026, 6, 1));

        assertThat(contractTermDueDateService.dueDate(term, notice, notice.getContract(), null))
                .contains(LocalDate.of(2026, 6, 1));
    }

    @Test
    void lboPlusTenWorkingDaysIsNinthOfApril() {
        PlanScheduleEvent event = dataManager.create(PlanScheduleEvent.class);
        event.setFinYear(2025);
        event.setLboDate(LocalDate.of(2025, 3, 26));
        event.setApprovalDate(LocalDate.of(2025, 4, 10));

        ContractTerm term = new ContractTerm();
        term.setContractLegacyId(18);
        term.setTermType(ContractTermType.DELIVERY);
        DeadlineSpec spec = new DeadlineSpec();
        spec.setAnchor(DeadlineAnchor.LBO_DATE);
        spec.setOffsetValue(10);
        spec.setUnit(DayCountUnit.WORKING_DAYS);
        spec.setStartNextDay(true);
        spec.setShiftRule(ShiftRule.NONE);
        term.setTerm(spec);

        assertThat(contractTermDueDateService.dueDate(term, null, null, event))
                .contains(LocalDate.of(2025, 4, 9));
    }

    @Test
    void uniqueContractLegacyIdAndTermType() {
        ContractTerm first = formulaTerm(10, 777001, "0173100017099000001");
        first.setFixedDate(LocalDate.of(2026, 1, 10));
        first.setTerm(null);
        ContractTerm saved = dataManager.save(first);
        try {
            ContractTerm duplicate = formulaTerm(10, 777001, "0173100017099000001");
            duplicate.setFixedDate(LocalDate.of(2026, 1, 11));
            duplicate.setTerm(null);
            assertThatThrownBy(() -> dataManager.save(duplicate))
                    .isInstanceOf(UniqueConstraintViolationException.class);
        } finally {
            dataManager.remove(dataManager.load(ContractTerm.class).id(saved.getId()).one());
        }
    }

    private static ContractTerm formulaTerm(int offset, Integer contractLegacyId, String purchaseNumber) {
        ContractTerm term = new ContractTerm();
        term.setId(UUID.randomUUID());
        term.setContractLegacyId(contractLegacyId);
        term.setPurchaseNumber(purchaseNumber);
        term.setTermType(ContractTermType.DELIVERY);
        DeadlineSpec spec = new DeadlineSpec();
        spec.setAnchor(DeadlineAnchor.CONTRACT_SIGNED);
        spec.setOffsetValue(offset);
        spec.setUnit(DayCountUnit.CALENDAR_DAYS);
        spec.setStartNextDay(true);
        spec.setShiftRule(ShiftRule.NEXT_WORKDAY);
        term.setTerm(spec);
        return term;
    }
}
