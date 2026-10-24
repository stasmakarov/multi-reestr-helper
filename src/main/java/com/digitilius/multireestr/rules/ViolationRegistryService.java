package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ViolationRegistryService {

    private final DataManager dataManager;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final FetchPlans fetchPlans;
    private final Metadata metadata;

    public ViolationRegistryService(
            DataManager dataManager,
            UnconstrainedDataManager unconstrainedDataManager,
            FetchPlans fetchPlans,
            Metadata metadata
    ) {
        this.dataManager = dataManager;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.fetchPlans = fetchPlans;
        this.metadata = metadata;
    }

    @Transactional
    public void mergeNotice(CheckRun checkRun, NoticeDto notice, NoticeCheckOutcome outcome) {
        if (notice == null) {
            throw new IllegalArgumentException("Нет извещения для записи в реестр");
        }
        merge(
                checkRun,
                new ObjectIdentity(
                        RuleObjectType.NOTICE,
                        notice.getNumber(),
                        notice.getId(),
                        ViolationMerge.objectCaption(notice)
                ),
                outcome,
                Map.of()
        );
    }

    @Transactional
    public void merge(
            CheckRun checkRun,
            ObjectIdentity object,
            NoticeCheckOutcome outcome,
            Map<String, DeadlineSnapshot> deadlines
    ) {
        if (checkRun == null || checkRun.getId() == null) {
            throw new IllegalArgumentException("Нет запуска проверки");
        }
        if (object == null || object.objectKey() == null || object.objectKey().isBlank()) {
            throw new IllegalArgumentException("Нет объекта для записи в реестр");
        }
        NoticeCheckOutcome safe = outcome == null ? NoticeCheckOutcome.empty() : outcome;
        Map<String, DeadlineSnapshot> extra = deadlines == null ? Map.of() : deadlines;
        CheckRun run = dataManager.load(CheckRun.class).id(checkRun.getId()).one();
        OffsetDateTime now = OffsetDateTime.now();
        List<Violation> existing = loadForObject(object.objectType(), object.objectKey());
        Map<String, Violation> byFingerprint = new HashMap<>();
        for (Violation violation : existing) {
            byFingerprint.put(violation.getFingerprint(), violation);
        }
        Map<String, CheckResultDto> uniqueHits = uniqueHitsByFingerprint(safe.hits());
        uniqueHits.entrySet().removeIf(entry -> safe.bindings().get(entry.getKey()) == null);
        Set<String> currentFingerprints = uniqueHits.keySet();
        SaveContext saveContext = new SaveContext();
        for (CheckResultDto hit : uniqueHits.values()) {
            NoticeCheckOutcome.HitBinding binding = safe.bindings().get(hit.getFingerprint());
            if (binding == null) {
                continue;
            }
            Violation violation = byFingerprint.get(hit.getFingerprint());
            if (violation == null) {
                violation = metadata.create(Violation.class);
                ViolationMerge.initNew(
                        violation, hit, binding.rule(), binding.version(),
                        object.legacyId(), object.caption(), run, now);
            } else {
                ViolationMerge.applyExistingHit(
                        violation, hit, binding.rule(), binding.version(),
                        object.legacyId(), object.caption(), run, now);
            }
            DeadlineSnapshot snapshot = extra.get(hit.getFingerprint());
            if (snapshot != null) {
                ViolationMerge.applyDeadline(violation, snapshot.dueDate(), snapshot.factDate(), snapshot.deviationDays());
            }
            saveContext.saving(violation);
        }
        for (Violation violation : existing) {
            if (ViolationMerge.shouldMarkFixed(violation, safe.evaluatedRuleIds(), currentFingerprints)) {
                ViolationMerge.markFixed(violation, now);
                saveContext.saving(violation);
            }
        }
        unconstrainedDataManager.save(saveContext);
    }

    private static Map<String, CheckResultDto> uniqueHitsByFingerprint(List<CheckResultDto> hits) {
        Map<String, CheckResultDto> unique = new LinkedHashMap<>();
        for (CheckResultDto hit : hits) {
            if (hit.getFingerprint() == null || hit.getFingerprint().isBlank()) {
                continue;
            }
            unique.putIfAbsent(hit.getFingerprint(), hit);
        }
        return unique;
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

    public record ObjectIdentity(
            RuleObjectType objectType,
            String objectKey,
            Integer legacyId,
            String caption
    ) {
    }

    public record DeadlineSnapshot(
            LocalDate dueDate,
            LocalDate factDate,
            Integer deviationDays
    ) {
    }
}
