package com.digitilius.multireestr.deadline;

import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class DeadlineAnchorResolver {

    public Optional<LocalDate> resolve(
            DeadlineAnchor anchor,
            NoticeDto notice,
            ContractDto contract,
            PlanScheduleEvent event
    ) {
        if (anchor == null) {
            return Optional.empty();
        }
        ContractDto resolvedContract = contract != null
                ? contract
                : notice == null ? null : notice.getContract();
        return switch (anchor) {
            case LBO_DATE -> optional(event, PlanScheduleEvent::getLboDate);
            case PLAN_APPROVAL_DATE -> optional(event, PlanScheduleEvent::getApprovalDate);
            case NOTICE_PUBLISHED -> localDate(notice, NoticeDto::getApplicationsFrom);
            case NOTICE_CHANGE_PUBLISHED -> noticeChangePublished(notice);
            case APPLICATIONS_END -> localDate(notice, NoticeDto::getApplicationsTo);
            case CONTRACT_SIGNED -> optional(resolvedContract, ContractDto::getDate);
            case DELIVERY_DOCUMENT -> firstExecution(resolvedContract, ExecutionKind.ACCEPTANCE, ExecutionDto::getDocDate);
            case ACCEPTANCE -> firstExecution(resolvedContract, ExecutionKind.ACCEPTANCE, ExecutionDto::getAcceptDate);
            case PAYMENT -> firstExecution(resolvedContract, ExecutionKind.PAYMENT, ExecutionDto::getDocDate);
            case STAGE_END -> firstStageEnd(resolvedContract);
        };
    }

    private static Optional<LocalDate> noticeChangePublished(NoticeDto notice) {
        if (notice == null || notice.getPublishedAt() == null) {
            return Optional.empty();
        }
        Integer version = notice.getModifVersion();
        if (version == null || version <= 1) {
            return Optional.empty();
        }
        return Optional.of(notice.getPublishedAt().toLocalDate());
    }

    private static Optional<LocalDate> firstExecution(
            ContractDto contract,
            ExecutionKind kind,
            Function<ExecutionDto, LocalDate> date
    ) {
        if (contract == null || contract.getExecutions() == null) {
            return Optional.empty();
        }
        return contract.getExecutions().stream()
                .filter(execution -> execution.getKind() == kind)
                .map(date)
                .filter(value -> value != null)
                .findFirst();
    }

    private static Optional<LocalDate> firstStageEnd(ContractDto contract) {
        if (contract == null || contract.getExecutions() == null) {
            return Optional.empty();
        }
        return contract.getExecutions().stream()
                .map(ExecutionDto::getStageEndDate)
                .filter(value -> value != null)
                .findFirst();
    }

    private static <T> Optional<LocalDate> optional(T source, Function<T, LocalDate> getter) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getter.apply(source));
    }

    private static Optional<LocalDate> localDate(NoticeDto notice, Function<NoticeDto, java.time.LocalDateTime> getter) {
        if (notice == null) {
            return Optional.empty();
        }
        java.time.LocalDateTime value = getter.apply(notice);
        return value == null ? Optional.empty() : Optional.of(value.toLocalDate());
    }
}
