package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DryRunDiff;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleParameter;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.RuleVersionStatus;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.scan.ContractTermDeadlineScan;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@RequireDocker
@TestPropertySource(properties = "mtr.scan.notice-lookback-days=1200")
class DryRunServiceLegacyTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    DryRunService dryRunService;
    @Autowired
    RuleVersionService ruleVersionService;
    @Autowired
    DataManager dataManager;

    @Test
    void noticeDraftDoesNotWriteRegistryAndMarksDisappeared() {
        RuleVersion draft = ruleVersionService.copyToDraft(loadActive("R03_PP1875"));
        CheckRun run = null;
        int violationsBefore = countViolations();
        try {
            clearOkpdList(draft);
            draft = dataManager.load(RuleVersion.class).id(draft.getId()).one();
            DryRunService.DryRunResult result = dryRunService.run(
                    draft, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31));
            run = result.checkRun();
            assertThat(run.getMode()).isEqualTo(CheckRunMode.DRY_RUN);
            assertThat(run.getStatus()).isIn(CheckRunStatus.SUCCESS, CheckRunStatus.PARTIAL);
            assertThat(run.getObjectsChecked()).isGreaterThanOrEqualTo(4);
            assertThat(countViolations()).isEqualTo(violationsBefore);
            assertThat(result.rows()).allMatch(row -> row.getDiff() != null);
            Set<String> disappeared = result.rows().stream()
                    .filter(row -> row.getDiff() == DryRunDiff.DISAPPEARED)
                    .map(CheckResultDto::getObjectKey)
                    .collect(Collectors.toSet());
            assertThat(disappeared).contains("0173100017026000001", "0173100017024000010");
            assertThat(result.rows()).noneMatch(row ->
                    "0173100017024000008".equals(row.getObjectKey()) && "R03_PP1875".equals(row.getRuleCode()));
        } finally {
            removeDraft(draft);
        }
    }

    @Test
    void enabledFalseStillRunsAndUnchangedWhenParamsSame() {
        RuleVersion draft = ruleVersionService.copyToDraft(loadActive("R03_PP1875"));
        UUID ruleId = draft.getRule().getId();
        int violationsBefore = countViolations();
        try {
            Rule rule = dataManager.load(Rule.class).id(ruleId).one();
            rule.setEnabled(false);
            dataManager.save(rule);
            DryRunService.DryRunResult result = dryRunService.run(
                    draft, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            assertThat(result.checkRun().getStatus()).isIn(CheckRunStatus.SUCCESS, CheckRunStatus.PARTIAL);
            assertThat(countViolations()).isEqualTo(violationsBefore);
            assertThat(result.rows())
                    .filteredOn(row -> "0173100017026000001".equals(row.getObjectKey()))
                    .isNotEmpty()
                    .allMatch(row -> row.getDiff() == DryRunDiff.UNCHANGED);
        } finally {
            Rule rule = dataManager.load(Rule.class).id(ruleId).one();
            rule.setEnabled(true);
            dataManager.save(rule);
            removeDraft(draft);
        }
    }

    @Test
    void contractDraftDoesNotWriteRegistry() {
        RuleVersion draft = ruleVersionService.copyToDraft(loadActive(ContractTermDeadlineScan.R09_DELIVERY));
        int overdueId = 99019;
        ContractTerm overdue = overdueTerm(overdueId);
        CheckRun run = null;
        int violationsBefore = countViolations();
        try {
            DryRunService.DryRunResult result = dryRunService.run(
                    draft, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
            run = result.checkRun();
            assertThat(run.getMode()).isEqualTo(CheckRunMode.DRY_RUN);
            assertThat(run.getObjectType().name()).isEqualTo("CONTRACT");
            assertThat(countViolations()).isEqualTo(violationsBefore);
            assertThat(result.rows())
                    .filteredOn(row -> String.valueOf(overdueId).equals(row.getObjectKey()))
                    .isNotEmpty()
                    .allMatch(row -> row.getDiff() == DryRunDiff.UNCHANGED);
        } finally {
            dataManager.remove(overdue);
            removeDraft(draft);
        }
    }

    private void clearOkpdList(RuleVersion draft) {
        List<RuleParameter> parameters = dataManager.load(RuleParameter.class)
                .query("select e from mtr_RuleParameter e where e.ruleVersion = :version")
                .parameter("version", draft)
                .list();
        SaveContext saveContext = new SaveContext();
        for (RuleParameter parameter : parameters) {
            if ("okpdList".equals(parameter.getCode())) {
                parameter.setReferenceList(null);
                saveContext.saving(parameter);
            }
        }
        dataManager.save(saveContext);
    }

    private RuleVersion loadActive(String code) {
        Rule rule = dataManager.load(Rule.class)
                .query("select e from mtr_Rule e where e.code = :code")
                .parameter("code", code)
                .one();
        return dataManager.load(RuleVersion.class)
                .query("select e from mtr_RuleVersion e where e.rule = :rule and e.status = :status")
                .parameter("rule", rule)
                .parameter("status", RuleVersionStatus.ACTIVE)
                .one();
    }

    private ContractTerm overdueTerm(int legacyId) {
        ContractTerm term = dataManager.create(ContractTerm.class);
        term.setContractLegacyId(legacyId);
        term.setTermType(ContractTermType.DELIVERY);
        term.setFixedDate(LocalDate.of(2026, 1, 1));
        return dataManager.save(term);
    }

    private int countViolations() {
        return dataManager.load(Violation.class).all().list().size();
    }

    private void removeDraft(RuleVersion draft) {
        if (draft == null || draft.getId() == null) {
            return;
        }
        SaveContext saveContext = new SaveContext();
        List<CheckRun> runs = dataManager.load(CheckRun.class)
                .query("select e from mtr_CheckRun e where e.ruleVersion = :version")
                .parameter("version", draft)
                .list();
        for (CheckRun run : runs) {
            saveContext.removing(run);
        }
        List<RuleParameter> parameters = dataManager.load(RuleParameter.class)
                .query("select e from mtr_RuleParameter e where e.ruleVersion = :version")
                .parameter("version", draft)
                .list();
        for (RuleParameter parameter : parameters) {
            saveContext.removing(parameter);
        }
        dataManager.save(saveContext);
        dataManager.remove(dataManager.load(RuleVersion.class).id(draft.getId()).one());
    }
}
