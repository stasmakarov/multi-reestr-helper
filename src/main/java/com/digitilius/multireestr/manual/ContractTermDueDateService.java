package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.deadline.DeadlineAnchorResolver;
import com.digitilius.multireestr.deadline.DeadlineCalculator;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.contract.ContractLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ContractTermDueDateService {

    private final DeadlineCalculator deadlineCalculator;
    private final DeadlineAnchorResolver deadlineAnchorResolver;
    private final NoticeCardService noticeCardService;
    private final ContractLookupService contractLookupService;
    private final DataManager dataManager;

    public ContractTermDueDateService(
            DeadlineCalculator deadlineCalculator,
            DeadlineAnchorResolver deadlineAnchorResolver,
            NoticeCardService noticeCardService,
            ContractLookupService contractLookupService,
            DataManager dataManager
    ) {
        this.deadlineCalculator = deadlineCalculator;
        this.deadlineAnchorResolver = deadlineAnchorResolver;
        this.noticeCardService = noticeCardService;
        this.contractLookupService = contractLookupService;
        this.dataManager = dataManager;
    }

    public Optional<LocalDate> dueDate(ContractTerm term) {
        NoticeDto notice = loadNotice(term);
        ContractDto contract = notice != null ? notice.getContract() : loadContract(term);
        return dueDate(term, notice, contract, loadPlanEvent(notice));
    }

    public Optional<LocalDate> dueDate(
            ContractTerm term,
            NoticeDto notice,
            ContractDto contract,
            PlanScheduleEvent event
    ) {
        if (term.getFixedDate() != null) {
            return Optional.of(term.getFixedDate());
        }
        if (!ContractTermRules.isFormulaFilled(term.getTerm())) {
            return Optional.empty();
        }
        return deadlineAnchorResolver.resolve(term.getTerm().getAnchor(), notice, contract, event)
                .map(anchor -> deadlineCalculator.deadline(anchor, term.getTerm()));
    }

    private NoticeDto loadNotice(ContractTerm term) {
        if (term.getPurchaseNumber() == null) {
            return null;
        }
        return NoticeNumber.tryParse(term.getPurchaseNumber())
                .flatMap(noticeCardService::loadOriginal)
                .orElse(null);
    }

    private ContractDto loadContract(ContractTerm term) {
        return contractLookupService.findById(term.getContractLegacyId()).orElse(null);
    }

    private PlanScheduleEvent loadPlanEvent(NoticeDto notice) {
        Integer year = finYear(notice);
        if (year == null) {
            return null;
        }
        List<PlanScheduleEvent> events = dataManager.load(PlanScheduleEvent.class)
                .query("select e from mtr_PlanScheduleEvent e where e.finYear = :year order by e.planVersion")
                .parameter("year", year)
                .list();
        return events.isEmpty() ? null : events.get(0);
    }

    private static Integer finYear(NoticeDto notice) {
        if (notice == null) {
            return null;
        }
        if (notice.getPublishedAt() != null) {
            return notice.getPublishedAt().getYear();
        }
        if (notice.getFinances() == null || notice.getFinances().isEmpty()) {
            return null;
        }
        return notice.getFinances().get(0).getFinYear();
    }
}
