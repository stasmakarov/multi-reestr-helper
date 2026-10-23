package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class NoticeRuleEvaluator {

    private final DataManager dataManager;
    private final FetchPlans fetchPlans;
    private final Map<RuleTemplateType, RuleTemplateEvaluator> evaluators;

    public NoticeRuleEvaluator(
            DataManager dataManager,
            FetchPlans fetchPlans,
            List<RuleTemplateEvaluator> evaluatorList
    ) {
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        Map<RuleTemplateType, RuleTemplateEvaluator> map = new EnumMap<>(RuleTemplateType.class);
        for (RuleTemplateEvaluator evaluator : evaluatorList) {
            map.put(evaluator.templateType(), evaluator);
        }
        this.evaluators = Map.copyOf(map);
    }

    public List<CheckResultDto> evaluate(NoticeDto notice) {
        return evaluateAll(notice).hits();
    }

    public List<CheckResultDto> evaluateVersion(NoticeDto notice, RuleVersion version) {
        if (notice == null || notice.getPublishedAt() == null || version == null || version.getRule() == null) {
            return List.of();
        }
        Rule rule = version.getRule();
        RuleTemplateEvaluator evaluator = evaluators.get(rule.getTemplateType());
        if (evaluator == null) {
            throw new IllegalStateException("Нет бина шаблона " + rule.getTemplateType());
        }
        return evaluator.evaluate(notice, rule, version, new RuleEvalParams(version.getParameters()));
    }

    public NoticeCheckOutcome evaluateAll(NoticeDto notice) {
        if (notice == null || notice.getPublishedAt() == null) {
            return NoticeCheckOutcome.empty();
        }
        List<CheckResultDto> results = new ArrayList<>();
        Set<UUID> evaluatedRuleIds = new LinkedHashSet<>();
        Map<String, NoticeCheckOutcome.HitBinding> bindings = new LinkedHashMap<>();
        LocalDate date = notice.getPublishedAt().toLocalDate();
        for (Rule rule : loadEnabledRules()) {
            RuleVersion version = activeVersionOn(rule, date);
            if (version == null) {
                continue;
            }
            RuleTemplateEvaluator evaluator = evaluators.get(rule.getTemplateType());
            if (evaluator == null) {
                continue;
            }
            evaluatedRuleIds.add(rule.getId());
            List<CheckResultDto> hits = evaluator.evaluate(notice, rule, version, new RuleEvalParams(version.getParameters()));
            for (CheckResultDto hit : hits) {
                results.add(hit);
                bindings.put(hit.getFingerprint(), new NoticeCheckOutcome.HitBinding(rule, version));
            }
        }
        return new NoticeCheckOutcome(List.copyOf(results), Set.copyOf(evaluatedRuleIds), Map.copyOf(bindings));
    }

    public static RuleVersion activeVersionOn(Rule rule, LocalDate date) {
        if (rule.getVersions() == null) {
            return null;
        }
        RuleVersion chosen = null;
        for (RuleVersion version : rule.getVersions()) {
            if (version == null || version.getStatus() != RuleVersionStatus.ACTIVE) {
                continue;
            }
            if (version.getValidFrom() == null || date.isBefore(version.getValidFrom())) {
                continue;
            }
            if (version.getValidTo() != null && date.isAfter(version.getValidTo())) {
                continue;
            }
            if (chosen == null || versionNumber(version) > versionNumber(chosen)) {
                chosen = version;
            }
        }
        return chosen;
    }

    private static int versionNumber(RuleVersion version) {
        return version.getVersionNumber() == null ? Integer.MIN_VALUE : version.getVersionNumber();
    }

    private List<Rule> loadEnabledRules() {
        FetchPlan fetchPlan = fetchPlans.builder(Rule.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("versions", versions -> versions
                        .addFetchPlan(FetchPlan.BASE)
                        .add("parameters", parameters -> parameters
                                .addFetchPlan(FetchPlan.BASE)
                                .add("referenceList", list -> list
                                        .addFetchPlan(FetchPlan.BASE)
                                        .add("items", FetchPlan.BASE))))
                .build();
        return dataManager.load(Rule.class)
                .query("select e from mtr_Rule e where e.enabled = true order by e.code")
                .fetchPlan(fetchPlan)
                .list();
    }
}
