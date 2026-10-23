package com.digitilius.multireestr.deadline;

import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeadlineAnchorResolver {

    public LocalDate resolve(DeadlineAnchor anchor, NoticeDto notice, ContractDto contract, PlanScheduleEvent plan) {
        if (anchor == null) {
            return null;
        }
        return switch (anchor) {
            case LBO_DATE -> plan == null ? null : plan.getLboDate();
            case PLAN_APPROVAL_DATE -> plan == null ? null : plan.getApprovalDate();
            case NOTICE_PUBLISHED -> noticePublished(notice);
            case NOTICE_CHANGE_PUBLISHED -> noticeChangePublished(notice);
            case APPLICATIONS_END -> toDate(notice == null ? null : notice.getApplicationsTo());
            case CONTRACT_SIGNED -> contract == null ? null : contract.getDate();
            case DELIVERY_DOCUMENT -> firstAcceptance(contract, ExecutionDto::getDocDate);
            case ACCEPTANCE -> firstAcceptance(contract, ExecutionDto::getAcceptDate);
            case PAYMENT -> firstPayment(contract);
            case STAGE_END -> firstStageEnd(contract);
        };
    }

    private static LocalDate noticePublished(NoticeDto notice) {
        if (notice == null) {
            return null;
        }
        LocalDate fromProcedure = toDate(notice.getApplicationsFrom());
        if (fromProcedure != null) {
            return fromProcedure;
        }
        return toDate(notice.getPublishedAt());
    }

    private static LocalDate noticeChangePublished(NoticeDto notice) {
        if (notice == null || notice.getModifVersion() == null || notice.getModifVersion() <= 1) {
            return null;
        }
        return toDate(notice.getPublishedAt());
    }

    private static LocalDate firstAcceptance(ContractDto contract, java.util.function.Function<ExecutionDto, LocalDate> getter) {
        if (contract == null || contract.getExecutions() == null) {
            return null;
        }
        for (ExecutionDto execution : contract.getExecutions()) {
            if (execution.getKind() == ExecutionKind.ACCEPTANCE) {
                LocalDate value = getter.apply(execution);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private static LocalDate firstPayment(ContractDto contract) {
        if (contract == null || contract.getExecutions() == null) {
            return null;
        }
        for (ExecutionDto execution : contract.getExecutions()) {
            if (execution.getKind() == ExecutionKind.PAYMENT && execution.getDocDate() != null) {
                return execution.getDocDate();
            }
        }
        return null;
    }

    private static LocalDate firstStageEnd(ContractDto contract) {
        List<ExecutionDto> executions = contract == null ? null : contract.getExecutions();
        if (executions == null) {
            return null;
        }
        for (ExecutionDto execution : executions) {
            if (execution.getStageEndDate() != null) {
                return execution.getStageEndDate();
            }
        }
        return null;
    }

    private static LocalDate toDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }
}
