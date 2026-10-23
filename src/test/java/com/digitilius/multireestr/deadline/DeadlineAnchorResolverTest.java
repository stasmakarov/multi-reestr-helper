package com.digitilius.multireestr.deadline;

import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadlineAnchorResolverTest {

    private final DeadlineAnchorResolver resolver = new DeadlineAnchorResolver();

    @Test
    void resolvesFromNoticeContractAndPlan() {
        NoticeDto notice = new NoticeDto();
        notice.setPublishedAt(LocalDateTime.of(2026, 2, 26, 12, 0));
        notice.setModifVersion(2);
        notice.setApplicationsFrom(LocalDateTime.of(2026, 2, 20, 9, 0));
        notice.setApplicationsTo(LocalDateTime.of(2026, 3, 3, 18, 0));

        ExecutionDto acceptance = new ExecutionDto();
        acceptance.setKind(ExecutionKind.ACCEPTANCE);
        acceptance.setDocDate(LocalDate.of(2026, 1, 23));
        acceptance.setAcceptDate(LocalDate.of(2026, 1, 24));
        acceptance.setStageEndDate(LocalDate.of(2026, 1, 20));
        ExecutionDto payment = new ExecutionDto();
        payment.setKind(ExecutionKind.PAYMENT);
        payment.setDocDate(LocalDate.of(2026, 2, 1));

        ContractDto contract = new ContractDto();
        contract.setDate(LocalDate.of(2025, 12, 23));
        contract.setExecutions(List.of(acceptance, payment));

        PlanScheduleEvent plan = new PlanScheduleEvent();
        plan.setLboDate(LocalDate.of(2025, 3, 26));
        plan.setApprovalDate(LocalDate.of(2025, 4, 10));

        assertThat(resolver.resolve(DeadlineAnchor.LBO_DATE, notice, contract, plan))
                .isEqualTo(LocalDate.of(2025, 3, 26));
        assertThat(resolver.resolve(DeadlineAnchor.PLAN_APPROVAL_DATE, notice, contract, plan))
                .isEqualTo(LocalDate.of(2025, 4, 10));
        assertThat(resolver.resolve(DeadlineAnchor.NOTICE_PUBLISHED, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 2, 20));
        assertThat(resolver.resolve(DeadlineAnchor.NOTICE_CHANGE_PUBLISHED, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 2, 26));
        assertThat(resolver.resolve(DeadlineAnchor.APPLICATIONS_END, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 3, 3));
        assertThat(resolver.resolve(DeadlineAnchor.CONTRACT_SIGNED, notice, contract, plan))
                .isEqualTo(LocalDate.of(2025, 12, 23));
        assertThat(resolver.resolve(DeadlineAnchor.DELIVERY_DOCUMENT, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 1, 23));
        assertThat(resolver.resolve(DeadlineAnchor.ACCEPTANCE, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 1, 24));
        assertThat(resolver.resolve(DeadlineAnchor.PAYMENT, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(resolver.resolve(DeadlineAnchor.STAGE_END, notice, contract, plan))
                .isEqualTo(LocalDate.of(2026, 1, 20));
    }

    @Test
    void missingSourcesYieldNull() {
        NoticeDto firstEdition = new NoticeDto();
        firstEdition.setModifVersion(1);
        firstEdition.setPublishedAt(LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThat(resolver.resolve(DeadlineAnchor.CONTRACT_SIGNED, null, null, null)).isNull();
        assertThat(resolver.resolve(DeadlineAnchor.NOTICE_CHANGE_PUBLISHED, firstEdition, null, null)).isNull();
        assertThat(resolver.resolve(null, null, null, null)).isNull();
    }
}
