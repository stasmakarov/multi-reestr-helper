package com.digitilius.multireestr.scan;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.LegacyDbInfo;
import com.digitilius.multireestr.legacy.contract.ContractLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.rules.NoticeCheckOutcome;
import com.digitilius.multireestr.rules.NoticeRuleEvaluator;
import com.digitilius.multireestr.rules.ViolationRegistryService;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ScheduledCheckService {

    static final int ERROR_MESSAGE_MAX = 2000;

    private final DataManager dataManager;
    private final FetchPlans fetchPlans;
    private final Metadata metadata;
    private final LegacyDbInfo legacyDbInfo;
    private final ScanProperties scanProperties;
    private final Clock clock;
    private final ContractTermDeadlineScan contractTermDeadlineScan;
    private final NoticeCardService noticeCardService;
    private final NoticeLookupService noticeLookupService;
    private final ContractLookupService contractLookupService;
    private final NoticeRuleEvaluator noticeRuleEvaluator;
    private final ViolationRegistryService violationRegistryService;

    public ScheduledCheckService(
            DataManager dataManager,
            FetchPlans fetchPlans,
            Metadata metadata,
            LegacyDbInfo legacyDbInfo,
            ScanProperties scanProperties,
            Clock clock,
            ContractTermDeadlineScan contractTermDeadlineScan,
            NoticeCardService noticeCardService,
            NoticeLookupService noticeLookupService,
            ContractLookupService contractLookupService,
            NoticeRuleEvaluator noticeRuleEvaluator,
            ViolationRegistryService violationRegistryService
    ) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.metadata = metadata;
        this.legacyDbInfo = legacyDbInfo;
        this.scanProperties = scanProperties;
        this.clock = clock;
        this.contractTermDeadlineScan = contractTermDeadlineScan;
        this.noticeCardService = noticeCardService;
        this.noticeLookupService = noticeLookupService;
        this.contractLookupService = contractLookupService;
        this.noticeRuleEvaluator = noticeRuleEvaluator;
        this.violationRegistryService = violationRegistryService;
    }

    public CheckRun runScheduled() {
        LocalDate today = LocalDate.now(clock);
        LocalDate periodFrom = today.minusDays(Math.max(0, scanProperties.getNoticeLookbackDays()));
        CheckRun run = metadata.create(CheckRun.class);
        run.setMode(CheckRunMode.SCHEDULED);
        run.setStatus(CheckRunStatus.RUNNING);
        run.setStartedAt(OffsetDateTime.now());
        run.setPeriodFrom(periodFrom);
        run.setPeriodTo(today);
        run = dataManager.save(run);

        List<String> errors = new ArrayList<>();
        int objectsChecked = 0;
        int violationsFound = 0;
        try {
            if (!legacyAvailable()) {
                return finish(run, CheckRunStatus.FAILED, 0, 0, "БД MultiReestr недоступна: " + legacyError());
            }

            Map<String, BoundRule> deadlineRules = loadDeadlineRules();
            if (!deadlineRules.containsKey(ContractTermDeadlineScan.R09_DELIVERY)
                    || !deadlineRules.containsKey(ContractTermDeadlineScan.R10_ACCEPTANCE)) {
                errors.add("нет правил R09_DELIVERY / R10_ACCEPTANCE");
            }

            TermScanStats terms = scanContractTerms(run, today, deadlineRules, errors);
            objectsChecked += terms.checked();
            violationsFound += terms.hits();

            NoticeScanStats notices = scanNotices(run, periodFrom, today, errors);
            objectsChecked += notices.checked();
            violationsFound += notices.hits();

            CheckRunStatus status;
            if (objectsChecked == 0 && !errors.isEmpty()) {
                status = CheckRunStatus.FAILED;
            } else if (!errors.isEmpty()) {
                status = CheckRunStatus.PARTIAL;
            } else {
                status = CheckRunStatus.SUCCESS;
            }
            return finish(run, status, objectsChecked, violationsFound, joinErrors(errors));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            errors.add(message);
            return finish(run, CheckRunStatus.FAILED, objectsChecked, violationsFound, joinErrors(errors));
        }
    }

    private TermScanStats scanContractTerms(
            CheckRun run,
            LocalDate today,
            Map<String, BoundRule> deadlineRules,
            List<String> errors
    ) {
        int checked = 0;
        int hits = 0;
        List<ContractTerm> terms = dataManager.load(ContractTerm.class)
                .query("select e from mtr_ContractTerm e where e.termType in :types")
                .parameter("types", List.of(ContractTermType.DELIVERY, ContractTermType.ACCEPTANCE))
                .list();
        Map<Integer, List<ContractTerm>> byContract = new LinkedHashMap<>();
        for (ContractTerm term : terms) {
            byContract.computeIfAbsent(term.getContractLegacyId(), key -> new ArrayList<>()).add(term);
        }
        for (Map.Entry<Integer, List<ContractTerm>> entry : byContract.entrySet()) {
            Integer contractId = entry.getKey();
            String label = "контракт " + contractId;
            try {
                List<CheckResultDto> hitList = new ArrayList<>();
                Map<String, NoticeCheckOutcome.HitBinding> bindings = new LinkedHashMap<>();
                Map<String, ViolationRegistryService.DeadlineSnapshot> deadlines = new LinkedHashMap<>();
                LinkedHashSet<UUID> evaluated = new LinkedHashSet<>();
                ContractDto contract = null;
                String caption = null;
                boolean anyEvaluated = false;
                for (ContractTerm term : entry.getValue()) {
                    try {
                        String code = ContractTermDeadlineScan.ruleCode(term.getTermType());
                        BoundRule bound = code == null ? null : deadlineRules.get(code);
                        if (bound == null) {
                            continue;
                        }
                        NoticeDto notice = loadNotice(term);
                        contract = notice != null && notice.getContract() != null
                                ? notice.getContract()
                                : contractLookupService.findById(term.getContractLegacyId()).orElse(contract);
                        PlanScheduleEvent event = loadPlanEvent(notice);
                        caption = contractCaption(term, contract);
                        ContractTermDeadlineScan.Assessment assessment = contractTermDeadlineScan.assess(
                                term, notice, contract, event, bound.rule(), bound.version(), today);
                        if (!assessment.evaluated()) {
                            continue;
                        }
                        anyEvaluated = true;
                        evaluated.add(assessment.rule().getId());
                        if (assessment.hit() != null) {
                            hitList.add(assessment.hit());
                            bindings.put(
                                    assessment.hit().getFingerprint(),
                                    new NoticeCheckOutcome.HitBinding(assessment.rule(), assessment.version()));
                            deadlines.put(
                                    assessment.hit().getFingerprint(),
                                    new ViolationRegistryService.DeadlineSnapshot(
                                            assessment.dueDate(), assessment.factDate(), assessment.deviationDays()));
                            hits++;
                        }
                    } catch (RuntimeException e) {
                        errors.add(label + "/" + term.getTermType() + ": " + shortMessage(e));
                    }
                }
                if (!anyEvaluated) {
                    continue;
                }
                checked++;
                NoticeCheckOutcome outcome = new NoticeCheckOutcome(
                        List.copyOf(hitList),
                        Set.copyOf(evaluated),
                        Map.copyOf(bindings)
                );
                violationRegistryService.merge(
                        run,
                        new ViolationRegistryService.ObjectIdentity(
                                RuleObjectType.CONTRACT,
                                String.valueOf(contractId),
                                contractId,
                                caption == null ? "контракт " + contractId : caption
                        ),
                        outcome,
                        deadlines
                );
            } catch (RuntimeException e) {
                errors.add(label + ": " + shortMessage(e));
            }
        }
        return new TermScanStats(checked, hits);
    }

    private NoticeScanStats scanNotices(CheckRun run, LocalDate from, LocalDate to, List<String> errors) {
        if (scanProperties.getMaxNotices() <= 0) {
            return new NoticeScanStats(0, 0);
        }
        int checked = 0;
        int hits = 0;
        List<NoticeCandidateDto> originals;
        try {
            originals = noticeLookupService.findOriginalsPublishedBetween(
                    from, to, scanProperties.getMaxNotices());
        } catch (RuntimeException e) {
            errors.add("извещения: " + shortMessage(e));
            return new NoticeScanStats(0, 0);
        }
        for (NoticeCandidateDto candidate : originals) {
            String number = candidate.getNumber();
            String label = "извещение " + (number == null ? candidate.getId() : number);
            try {
                NoticeDto notice = NoticeNumber.tryParse(number)
                        .flatMap(noticeCardService::loadOriginal)
                        .orElse(null);
                if (notice == null) {
                    errors.add(label + ": не загружено");
                    continue;
                }
                NoticeCheckOutcome outcome = noticeRuleEvaluator.evaluateAll(notice);
                checked++;
                hits += outcome.hits().size();
                violationRegistryService.mergeNotice(run, notice, outcome);
            } catch (RuntimeException e) {
                errors.add(label + ": " + shortMessage(e));
            }
        }
        return new NoticeScanStats(checked, hits);
    }

    private Map<String, BoundRule> loadDeadlineRules() {
        FetchPlan fetchPlan = fetchPlans.builder(Rule.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("versions", FetchPlan.BASE)
                .build();
        List<Rule> rules = dataManager.load(Rule.class)
                .query("select e from mtr_Rule e where e.code in :codes and e.enabled = true")
                .parameter("codes", List.of(ContractTermDeadlineScan.R09_DELIVERY, ContractTermDeadlineScan.R10_ACCEPTANCE))
                .fetchPlan(fetchPlan)
                .list();
        Map<String, BoundRule> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(clock);
        for (Rule rule : rules) {
            RuleVersion version = com.digitilius.multireestr.rules.NoticeRuleEvaluator.activeVersionOn(rule, today);
            if (version != null) {
                result.put(rule.getCode(), new BoundRule(rule, version));
            }
        }
        return result;
    }

    private NoticeDto loadNotice(ContractTerm term) {
        if (term.getPurchaseNumber() == null) {
            return null;
        }
        return NoticeNumber.tryParse(term.getPurchaseNumber())
                .flatMap(noticeCardService::loadOriginal)
                .orElse(null);
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

    private static String contractCaption(ContractTerm term, ContractDto contract) {
        if (term.getContractNumber() != null && !term.getContractNumber().isBlank()) {
            return term.getContractNumber();
        }
        if (contract != null && contract.getNumber() != null) {
            return contract.getNumber();
        }
        return "контракт " + term.getContractLegacyId();
    }

    private boolean legacyAvailable() {
        return legacyDbInfo.hasPool() && legacyDbInfo.check().ok();
    }

    private String legacyError() {
        String error = legacyDbInfo.check().error();
        return error == null || error.isBlank() ? "нет подключения" : error;
    }

    private CheckRun finish(
            CheckRun run,
            CheckRunStatus status,
            int objectsChecked,
            int violationsFound,
            String errorMessage
    ) {
        run.setStatus(status);
        run.setFinishedAt(OffsetDateTime.now());
        run.setObjectsChecked(objectsChecked);
        run.setViolationsFound(violationsFound);
        run.setErrorMessage(blankToNull(errorMessage));
        return dataManager.save(run);
    }

    private static String joinErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        String joined = String.join("; ", errors);
        if (joined.length() <= ERROR_MESSAGE_MAX) {
            return joined;
        }
        return joined.substring(0, ERROR_MESSAGE_MAX);
    }

    private static String shortMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        if (message.length() > 240) {
            return message.substring(0, 240);
        }
        return message;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record BoundRule(Rule rule, RuleVersion version) {
    }

    private record TermScanStats(int checked, int hits) {
    }

    private record NoticeScanStats(int checked, int hits) {
    }
}
