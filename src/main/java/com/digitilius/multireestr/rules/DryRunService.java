package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DryRunDiff;
import com.digitilius.multireestr.entity.PlanScheduleEvent;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.LegacyDbInfo;
import com.digitilius.multireestr.legacy.contract.ContractLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.scan.ContractTermDeadlineScan;
import com.digitilius.multireestr.scan.ScanProperties;
import com.digitilius.multireestr.security.MultiReestrSpecifics;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DryRunService {

    static final int ERROR_MESSAGE_MAX = 2000;

    private final DataManager dataManager;
    private final FetchPlans fetchPlans;
    private final Metadata metadata;
    private final AccessManager accessManager;
    private final LegacyDbInfo legacyDbInfo;
    private final ScanProperties scanProperties;
    private final Clock clock;
    private final NoticeLookupService noticeLookupService;
    private final NoticeCardService noticeCardService;
    private final NoticeRuleEvaluator noticeRuleEvaluator;
    private final ContractTermDeadlineScan contractTermDeadlineScan;
    private final ContractLookupService contractLookupService;

    public DryRunService(
            DataManager dataManager,
            FetchPlans fetchPlans,
            Metadata metadata,
            AccessManager accessManager,
            LegacyDbInfo legacyDbInfo,
            ScanProperties scanProperties,
            Clock clock,
            NoticeLookupService noticeLookupService,
            NoticeCardService noticeCardService,
            NoticeRuleEvaluator noticeRuleEvaluator,
            ContractTermDeadlineScan contractTermDeadlineScan,
            ContractLookupService contractLookupService
    ) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.metadata = metadata;
        this.accessManager = accessManager;
        this.legacyDbInfo = legacyDbInfo;
        this.scanProperties = scanProperties;
        this.clock = clock;
        this.noticeLookupService = noticeLookupService;
        this.noticeCardService = noticeCardService;
        this.noticeRuleEvaluator = noticeRuleEvaluator;
        this.contractTermDeadlineScan = contractTermDeadlineScan;
        this.contractLookupService = contractLookupService;
    }

    public boolean canDryRun() {
        SpecificOperationAccessContext context =
                new SpecificOperationAccessContext(MultiReestrSpecifics.CHECK_DRY_RUN);
        accessManager.applyRegisteredConstraints(context);
        return context.isPermitted();
    }

    public DryRunResult run(RuleVersion version, LocalDate periodFrom, LocalDate periodTo) {
        if (!canDryRun()) {
            throw new IllegalStateException("Нет права запускать пробный прогон");
        }
        if (periodFrom == null || periodTo == null) {
            throw new IllegalArgumentException("Нужен период публикации");
        }
        if (periodFrom.isAfter(periodTo)) {
            throw new IllegalArgumentException("Дата начала периода позже даты окончания");
        }
        long days = ChronoUnit.DAYS.between(periodFrom, periodTo);
        int maxDays = Math.max(0, scanProperties.getNoticeLookbackDays());
        if (days > maxDays) {
            throw new IllegalArgumentException("Период длиннее " + maxDays + " дней");
        }
        RuleVersion draft = loadEvalVersion(version == null ? null : version.getId());
        if (draft.getStatus() != RuleVersionStatus.DRAFT) {
            throw new IllegalStateException("Пробный прогон доступен только для черновика");
        }
        Rule rule = draft.getRule();
        RuleObjectType objectType = rule.getObjectType();
        if (objectType != RuleObjectType.NOTICE && objectType != RuleObjectType.CONTRACT) {
            throw new IllegalStateException("Тип объекта " + objectType + " не поддерживается");
        }
        if (objectType == RuleObjectType.CONTRACT) {
            String code = rule.getCode();
            if (!ContractTermDeadlineScan.R09_DELIVERY.equals(code)
                    && !ContractTermDeadlineScan.R10_ACCEPTANCE.equals(code)) {
                throw new IllegalStateException("Нет сканера для правила " + code);
            }
        }

        CheckRun run = metadata.create(CheckRun.class);
        run.setMode(CheckRunMode.DRY_RUN);
        run.setStatus(CheckRunStatus.RUNNING);
        run.setStartedAt(OffsetDateTime.now());
        run.setObjectType(objectType);
        run.setPeriodFrom(periodFrom);
        run.setPeriodTo(periodTo);
        run.setRuleVersion(draft);
        run = dataManager.save(run);

        List<String> errors = new ArrayList<>();
        int objectsChecked = 0;
        int draftHits = 0;
        boolean truncated = false;
        List<CheckResultDto> rows = new ArrayList<>();
        try {
            if (!legacyAvailable()) {
                CheckRun failed = finish(run, CheckRunStatus.FAILED, 0, 0,
                        "БД MultiReestr недоступна: " + legacyError());
                return new DryRunResult(failed, List.of(), false);
            }
            if (objectType == RuleObjectType.NOTICE) {
                NoticeScan scan = scanNotices(draft, periodFrom, periodTo, errors);
                objectsChecked = scan.checked();
                draftHits = scan.draftHits();
                truncated = scan.truncated();
                rows = scan.rows();
            } else {
                ContractScan scan = scanContracts(draft, errors);
                objectsChecked = scan.checked();
                draftHits = scan.draftHits();
                rows = scan.rows();
            }
            CheckRunStatus status;
            if (objectsChecked == 0 && !errors.isEmpty()) {
                status = CheckRunStatus.FAILED;
            } else if (!errors.isEmpty()) {
                status = CheckRunStatus.PARTIAL;
            } else {
                status = CheckRunStatus.SUCCESS;
            }
            CheckRun finished = finish(run, status, objectsChecked, draftHits, joinErrors(errors));
            return new DryRunResult(finished, List.copyOf(rows), truncated);
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            errors.add(message);
            CheckRun failed = finish(run, CheckRunStatus.FAILED, objectsChecked, draftHits, joinErrors(errors));
            return new DryRunResult(failed, List.copyOf(rows), truncated);
        }
    }

    private NoticeScan scanNotices(
            RuleVersion draft,
            LocalDate from,
            LocalDate to,
            List<String> errors
    ) {
        int cap = scanProperties.getMaxNotices();
        if (cap <= 0) {
            return new NoticeScan(0, 0, false, List.of());
        }
        List<NoticeCandidateDto> originals;
        try {
            originals = noticeLookupService.findOriginalsPublishedBetween(from, to, cap + 1);
        } catch (RuntimeException e) {
            errors.add("извещения: " + shortMessage(e));
            return new NoticeScan(0, 0, false, List.of());
        }
        boolean truncated = originals.size() > cap;
        if (truncated) {
            originals = originals.subList(0, cap);
        }
        int checked = 0;
        int draftHits = 0;
        List<CheckResultDto> rows = new ArrayList<>();
        Rule rule = draft.getRule();
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
                List<CheckResultDto> draftList = noticeRuleEvaluator.evaluateVersion(notice, draft);
                RuleVersion active = NoticeRuleEvaluator.activeVersionOn(rule, notice.getPublishedAt().toLocalDate());
                List<CheckResultDto> activeList = active == null
                        ? List.of()
                        : noticeRuleEvaluator.evaluateVersion(notice, active);
                checked++;
                draftHits += draftList.size();
                rows.addAll(DryRunDiffs.compare(draftList, activeList));
            } catch (RuntimeException e) {
                errors.add(label + ": " + shortMessage(e));
            }
        }
        return new NoticeScan(checked, draftHits, truncated, rows);
    }

    private ContractScan scanContracts(RuleVersion draft, List<String> errors) {
        ContractTermType termType = termType(draft.getRule().getCode());
        if (termType == null) {
            errors.add("нет типа условия для " + draft.getRule().getCode());
            return new ContractScan(0, 0, List.of());
        }
        LocalDate today = LocalDate.now(clock);
        RuleVersion active = NoticeRuleEvaluator.activeVersionOn(draft.getRule(), today);
        List<ContractTerm> terms = dataManager.load(ContractTerm.class)
                .query("select e from mtr_ContractTerm e where e.termType = :type")
                .parameter("type", termType)
                .list();
        int checked = 0;
        int draftHits = 0;
        List<CheckResultDto> rows = new ArrayList<>();
        for (ContractTerm term : terms) {
            String label = "контракт " + term.getContractLegacyId();
            try {
                NoticeDto notice = loadNotice(term);
                ContractDto contract = notice != null && notice.getContract() != null
                        ? notice.getContract()
                        : contractLookupService.findById(term.getContractLegacyId()).orElse(null);
                PlanScheduleEvent event = loadPlanEvent(notice);
                ContractTermDeadlineScan.Assessment draftAssessment = contractTermDeadlineScan.assess(
                        term, notice, contract, event, draft.getRule(), draft, today);
                List<CheckResultDto> draftList = hitsOf(draftAssessment);
                List<CheckResultDto> activeList = List.of();
                if (active != null) {
                    ContractTermDeadlineScan.Assessment activeAssessment = contractTermDeadlineScan.assess(
                            term, notice, contract, event, active.getRule(), active, today);
                    activeList = hitsOf(activeAssessment);
                    if (!draftAssessment.evaluated() && !activeAssessment.evaluated()) {
                        continue;
                    }
                } else if (!draftAssessment.evaluated()) {
                    continue;
                }
                checked++;
                draftHits += draftList.size();
                rows.addAll(DryRunDiffs.compare(draftList, activeList));
            } catch (RuntimeException e) {
                errors.add(label + ": " + shortMessage(e));
            }
        }
        return new ContractScan(checked, draftHits, rows);
    }

    private static List<CheckResultDto> hitsOf(ContractTermDeadlineScan.Assessment assessment) {
        if (assessment == null || assessment.hit() == null) {
            return List.of();
        }
        return List.of(assessment.hit());
    }

    private static ContractTermType termType(String code) {
        if (ContractTermDeadlineScan.R09_DELIVERY.equals(code)) {
            return ContractTermType.DELIVERY;
        }
        if (ContractTermDeadlineScan.R10_ACCEPTANCE.equals(code)) {
            return ContractTermType.ACCEPTANCE;
        }
        return null;
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

    private RuleVersion loadEvalVersion(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Нет версии правила");
        }
        FetchPlan fetchPlan = fetchPlans.builder(RuleVersion.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("parameters", parameters -> parameters
                        .addFetchPlan(FetchPlan.BASE)
                        .add("referenceList", list -> list
                                .addFetchPlan(FetchPlan.BASE)
                                .add("items", FetchPlan.BASE)))
                .add("rule", rule -> rule
                        .addFetchPlan(FetchPlan.BASE)
                        .add("versions", versions -> versions
                                .addFetchPlan(FetchPlan.BASE)
                                .add("parameters", parameters -> parameters
                                        .addFetchPlan(FetchPlan.BASE)
                                        .add("referenceList", list -> list
                                                .addFetchPlan(FetchPlan.BASE)
                                                .add("items", FetchPlan.BASE)))))
                .build();
        return dataManager.load(RuleVersion.class)
                .id(id)
                .fetchPlan(fetchPlan)
                .one();
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

    public record DryRunResult(CheckRun checkRun, List<CheckResultDto> rows, boolean truncated) {
        public long count(DryRunDiff diff) {
            return rows.stream().filter(row -> row.getDiff() == diff).count();
        }
    }

    private record NoticeScan(int checked, int draftHits, boolean truncated, List<CheckResultDto> rows) {
    }

    private record ContractScan(int checked, int draftHits, List<CheckResultDto> rows) {
    }
}
