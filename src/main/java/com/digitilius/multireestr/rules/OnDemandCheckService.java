package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.security.MultiReestrSpecifics;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.AccessManager;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OnDemandCheckService {

    private final DataManager dataManager;
    private final FetchPlans fetchPlans;
    private final Metadata metadata;
    private final NoticeRuleEvaluator noticeRuleEvaluator;
    private final AccessManager accessManager;
    private final CurrentAuthentication currentAuthentication;

    public OnDemandCheckService(
            DataManager dataManager,
            FetchPlans fetchPlans,
            Metadata metadata,
            NoticeRuleEvaluator noticeRuleEvaluator,
            AccessManager accessManager,
            CurrentAuthentication currentAuthentication
    ) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.metadata = metadata;
        this.noticeRuleEvaluator = noticeRuleEvaluator;
        this.accessManager = accessManager;
        this.currentAuthentication = currentAuthentication;
    }

    public boolean canCheckOnDemand() {
        return permitted(MultiReestrSpecifics.CHECK_ON_DEMAND);
    }

    public OnDemandCheckResult check(NoticeDto notice) {
        requireCheckPermission();
        if (notice == null) {
            throw new IllegalArgumentException("Нет извещения для проверки");
        }
        CheckRun run = metadata.create(CheckRun.class);
        run.setMode(CheckRunMode.ON_DEMAND);
        run.setStatus(CheckRunStatus.RUNNING);
        run.setStartedAt(OffsetDateTime.now());
        run.setObjectType(RuleObjectType.NOTICE);
        run.setObjectKey(notice.getNumber());
        run = dataManager.save(run);
        try {
            NoticeCheckOutcome outcome = noticeRuleEvaluator.evaluateAll(notice);
            run.setStatus(CheckRunStatus.SUCCESS);
            run.setFinishedAt(OffsetDateTime.now());
            run.setObjectsChecked(1);
            run.setViolationsFound(outcome.hits().size());
            run = dataManager.save(run);
            return new OnDemandCheckResult(run, outcome);
        } catch (RuntimeException e) {
            run.setStatus(CheckRunStatus.FAILED);
            run.setFinishedAt(OffsetDateTime.now());
            run.setErrorMessage(e.getMessage());
            dataManager.save(run);
            throw e;
        }
    }

    @Transactional
    public void saveToRegistry(CheckRun checkRun, NoticeDto notice, NoticeCheckOutcome outcome) {
        requireCheckPermission();
        if (checkRun == null || checkRun.getId() == null) {
            throw new IllegalArgumentException("Нет запуска проверки");
        }
        if (notice == null) {
            throw new IllegalArgumentException("Нет извещения для записи в реестр");
        }
        NoticeCheckOutcome safe = outcome == null ? NoticeCheckOutcome.empty() : outcome;
        CheckRun run = dataManager.load(CheckRun.class).id(checkRun.getId()).one();
        OffsetDateTime now = OffsetDateTime.now();
        List<Violation> existing = loadForObject(RuleObjectType.NOTICE, notice.getNumber());
        Map<String, Violation> byFingerprint = new HashMap<>();
        for (Violation violation : existing) {
            byFingerprint.put(violation.getFingerprint(), violation);
        }
        Set<String> currentFingerprints = safe.bindings().keySet();
        SaveContext saveContext = new SaveContext();
        for (CheckResultDto hit : safe.hits()) {
            NoticeCheckOutcome.HitBinding binding = safe.bindings().get(hit.getFingerprint());
            if (binding == null) {
                continue;
            }
            Violation violation = byFingerprint.get(hit.getFingerprint());
            if (violation == null) {
                violation = metadata.create(Violation.class);
                ViolationMerge.initNew(violation, hit, binding.rule(), binding.version(), notice, run, now);
            } else {
                ViolationMerge.applyExistingHit(
                        violation, hit, binding.rule(), binding.version(), notice, run, now);
            }
            saveContext.saving(violation);
        }
        for (Violation violation : existing) {
            if (ViolationMerge.shouldMarkFixed(violation, safe.evaluatedRuleIds(), currentFingerprints)) {
                ViolationMerge.markFixed(violation, now);
                saveContext.saving(violation);
            }
        }
        dataManager.save(saveContext);
    }

    @Transactional
    public Violation acknowledge(Violation violation) {
        Violation loaded = reload(violation.getId());
        if (loaded.getStatus() != ViolationStatus.NEW) {
            throw new IllegalStateException("Квитировать можно только новое нарушение");
        }
        loaded.setStatus(ViolationStatus.ACKNOWLEDGED);
        loaded.setAcknowledgedBy(currentAuthentication.getUser().getUsername());
        loaded.setAcknowledgedAt(OffsetDateTime.now());
        return dataManager.save(loaded);
    }

    @Transactional
    public Violation markFalsePositive(Violation violation) {
        String comment = violation.getComment();
        if (comment == null || comment.isBlank()) {
            throw new IllegalStateException("Комментарий обязателен для ложного срабатывания");
        }
        Violation loaded = reload(violation.getId());
        if (loaded.getStatus() == ViolationStatus.FIXED) {
            throw new IllegalStateException("Устранённое нарушение нельзя пометить ложным");
        }
        loaded.setComment(comment);
        loaded.setStatus(ViolationStatus.FALSE_POSITIVE);
        return dataManager.save(loaded);
    }

    private List<Violation> loadForObject(RuleObjectType objectType, String objectKey) {
        FetchPlan fetchPlan = fetchPlans.builder(Violation.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("rule", FetchPlan.BASE)
                .add("ruleVersion", FetchPlan.BASE)
                .build();
        return dataManager.load(Violation.class)
                .query("select e from mtr_Violation e where e.objectType = :type and e.objectKey = :key")
                .parameter("type", objectType)
                .parameter("key", objectKey)
                .fetchPlan(fetchPlan)
                .list();
    }

    private Violation reload(UUID id) {
        FetchPlan fetchPlan = fetchPlans.builder(Violation.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("rule", FetchPlan.INSTANCE_NAME)
                .add("ruleVersion", FetchPlan.INSTANCE_NAME)
                .add("lastCheckRun", FetchPlan.INSTANCE_NAME)
                .build();
        return dataManager.load(Violation.class)
                .id(id)
                .fetchPlan(fetchPlan)
                .one();
    }

    private void requireCheckPermission() {
        if (!canCheckOnDemand()) {
            throw new IllegalStateException("Нет права запускать проверку по запросу");
        }
    }

    private boolean permitted(String resource) {
        SpecificOperationAccessContext context = new SpecificOperationAccessContext(resource);
        accessManager.applyRegisteredConstraints(context);
        return context.isPermitted();
    }

    public record OnDemandCheckResult(CheckRun checkRun, NoticeCheckOutcome outcome) {
    }
}
