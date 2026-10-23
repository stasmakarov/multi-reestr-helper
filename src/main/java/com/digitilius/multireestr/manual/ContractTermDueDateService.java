package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.deadline.DeadlineAnchorResolver;
import com.digitilius.multireestr.deadline.DeadlineCalculator;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.DeadlineSpec;
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
    private final DeadlineAnchorResolver anchorResolver;
    private final ContractLookupService contractLookupService;
    private final NoticeCardService noticeCardService;
    private final DataManager dataManager;

    public ContractTermDueDateService(
            DeadlineCalculator deadlineCalculator,
            DeadlineAnchorResolver anchorResolver,
            ContractLookupService contractLookupService,
            NoticeCardService noticeCardService,
            DataManager dataManager
    ) {
        this.deadlineCalculator = deadlineCalculator;
        this.anchorResolver = anchorResolver;
        this.contractLookupService = contractLookupService;
        this.noticeCardService = noticeCardService;
        this.dataManager = dataManager;
    }

    public Optional<LocalDate> calculate(ContractTerm term) {
        if (term == null) {
            return Optional.empty();
        }
        if (term.getFixedDate() != null) {
            return Optional.of(term.getFixedDate());
        }
        DeadlineSpec spec = term.getTerm();
        if (!formulaReady(spec)) {
            return Optional.empty();
        }
        NoticeDto notice = loadNotice(term.getPurchaseNumber());
        ContractDto contract = loadContract(term, notice);
        PlanScheduleEvent plan = loadPlan(notice, contract);
        LocalDate anchor = anchorResolver.resolve(spec.getAnchor(), notice, contract, plan);
        if (anchor == null) {
            return Optional.empty();
        }
        return Optional.of(deadlineCalculator.deadline(anchor, spec));
    }

    public static boolean formulaReady(DeadlineSpec spec) {
        return spec != null
                && spec.getAnchor() != null
                && spec.getOffsetValue() != null
                && spec.getUnit() != null;
    }

    private NoticeDto loadNotice(String purchaseNumber) {
        if (purchaseNumber == null || purchaseNumber.isBlank()) {
            return null;
        }
        return NoticeNumber.tryParse(purchaseNumber)
                .flatMap(noticeCardService::loadOriginal)
                .orElse(null);
    }

    private ContractDto loadContract(ContractTerm term, NoticeDto notice) {
        if (notice != null && notice.getContract() != null) {
            return notice.getContract();
        }
        return contractLookupService.findById(term.getContractLegacyId()).orElse(null);
    }

    private PlanScheduleEvent loadPlan(NoticeDto notice, ContractDto contract) {
        Integer year = null;
        if (notice != null && notice.getPublishedAt() != null) {
            year = notice.getPublishedAt().getYear();
        } else if (contract != null && contract.getDate() != null) {
            year = contract.getDate().getYear();
        }
        if (year == null) {
            return null;
        }
        List<PlanScheduleEvent> found = dataManager.load(PlanScheduleEvent.class)
                .query("select e from mtr_PlanScheduleEvent e where e.finYear = :year order by e.planVersion desc")
                .parameter("year", year)
                .maxResults(1)
                .list();
        return found.isEmpty() ? null : found.get(0);
    }
}
