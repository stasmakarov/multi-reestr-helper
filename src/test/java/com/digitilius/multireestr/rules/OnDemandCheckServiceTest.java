package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.RuleTemplateType;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.Severity;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class OnDemandCheckServiceTest {

    @Autowired
    OnDemandCheckService onDemandCheckService;
    @Autowired
    DataManager dataManager;

    @Test
    void checkWritesJournalAndSaveDedupesAndFixesOnlyEvaluatedRules() {
        NoticeDto notice = batteryNotice();
        UUID stubRuleId = null;
        try {
        OnDemandCheckService.OnDemandCheckResult first = onDemandCheckService.check(notice);
        assertThat(first.checkRun().getMode()).isEqualTo(CheckRunMode.ON_DEMAND);
        assertThat(first.checkRun().getStatus()).isEqualTo(CheckRunStatus.SUCCESS);
        assertThat(first.checkRun().getObjectKey()).isEqualTo(notice.getNumber());
        assertThat(first.checkRun().getObjectsChecked()).isEqualTo(1);
        assertThat(first.outcome().hits()).extracting(hit -> hit.getRuleCode())
                .contains("R03_PP1875", "R07_NO_DESCRIPTION")
                .doesNotContain("R05_KTRU_CHARS");
        onDemandCheckService.saveToRegistry(first.checkRun(), notice, first.outcome());

        List<Violation> afterFirst = loadViolations(notice.getNumber());
        assertThat(afterFirst).hasSize(first.outcome().hits().size());
        assertThat(afterFirst).allMatch(v -> v.getStatus() == ViolationStatus.NEW);
        assertThat(afterFirst).allMatch(v -> v.getLegalRef() != null && !v.getLegalRef().isBlank());

        OnDemandCheckService.OnDemandCheckResult again = onDemandCheckService.check(notice);
        onDemandCheckService.saveToRegistry(again.checkRun(), notice, again.outcome());
        assertThat(loadViolations(notice.getNumber())).hasSize(afterFirst.size());

        Violation planted = plantUnevaluated(notice, first.checkRun(), afterFirst.get(0).getRuleVersion());
        stubRuleId = planted.getRule().getId();

        NoticeDto cleaned = batteryNotice();
        cleaned.getItems().get(0).setForeignBan(true);
        OnDemandCheckService.OnDemandCheckResult second = onDemandCheckService.check(cleaned);
        onDemandCheckService.saveToRegistry(second.checkRun(), cleaned, second.outcome());

        List<Violation> afterSecond = loadViolations(notice.getNumber());
        assertThat(afterSecond).hasSize(afterFirst.size() + 1);
        assertThat(afterSecond).filteredOn(v -> "R03_PP1875".equals(v.getRule().getCode()))
                .allMatch(v -> v.getStatus() == ViolationStatus.FIXED);
        assertThat(afterSecond).filteredOn(v -> "R07_NO_DESCRIPTION".equals(v.getRule().getCode()))
                .allMatch(v -> v.getStatus() == ViolationStatus.NEW);
        assertThat(dataManager.load(Violation.class).id(planted.getId()).one().getStatus())
                .isEqualTo(ViolationStatus.NEW);

        Violation r07 = afterSecond.stream()
                .filter(v -> "R07_NO_DESCRIPTION".equals(v.getRule().getCode()))
                .findFirst()
                .orElseThrow();
        Violation acknowledged = onDemandCheckService.acknowledge(r07);
        assertThat(acknowledged.getStatus()).isEqualTo(ViolationStatus.ACKNOWLEDGED);
        assertThat(acknowledged.getAcknowledgedBy()).isEqualTo("admin");

        acknowledged.setComment("не применимо");
        Violation falsePositive = onDemandCheckService.markFalsePositive(acknowledged);
        assertThat(falsePositive.getStatus()).isEqualTo(ViolationStatus.FALSE_POSITIVE);

        OnDemandCheckService.OnDemandCheckResult third = onDemandCheckService.check(cleaned);
        onDemandCheckService.saveToRegistry(third.checkRun(), cleaned, third.outcome());
        assertThat(dataManager.load(Violation.class).id(falsePositive.getId()).one().getStatus())
                .isEqualTo(ViolationStatus.FALSE_POSITIVE);

        } finally {
            if (notice.getNumber() != null) {
                removeViolationsAndRuns(notice.getNumber(), stubRuleId);
            }
        }
    }

    @Test
    void falsePositiveWithoutCommentFails() {
        Violation violation = dataManager.create(Violation.class);
        violation.setComment(" ");
        assertThatThrownBy(() -> onDemandCheckService.markFalsePositive(violation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Комментарий");
    }

    private Violation plantUnevaluated(NoticeDto notice, CheckRun run, RuleVersion anyVersion) {
        Rule stub = dataManager.create(Rule.class);
        stub.setCode("R02_DOC_" + UUID.randomUUID().toString().substring(0, 8));
        stub.setName("Документ");
        stub.setTemplateType(RuleTemplateType.DOCUMENT_STRUCTURE_MISMATCH);
        stub.setObjectType(RuleObjectType.NOTICE);
        stub.setSeverity(Severity.ERROR);
        stub.setEnabled(true);
        stub = dataManager.save(stub);

        Violation planted = dataManager.create(Violation.class);
        planted.setRule(stub);
        planted.setRuleVersion(anyVersion);
        planted.setLastCheckRun(run);
        planted.setObjectType(RuleObjectType.NOTICE);
        planted.setObjectKey(notice.getNumber());
        planted.setLegacyId(notice.getId());
        planted.setFingerprint(CheckResults.fingerprint(stub.getCode(), notice.getNumber(), "doc"));
        planted.setSeverity(Severity.ERROR);
        planted.setMessage("заглушка без бина");
        planted.setStatus(ViolationStatus.NEW);
        planted.setFirstDetectedAt(OffsetDateTime.now());
        planted.setLastDetectedAt(OffsetDateTime.now());
        return dataManager.save(planted);
    }

    private List<Violation> loadViolations(String objectKey) {
        return dataManager.load(Violation.class)
                .query("select e from mtr_Violation e join fetch e.rule join fetch e.ruleVersion where e.objectKey = :key")
                .parameter("key", objectKey)
                .list();
    }

    private void removeViolationsAndRuns(String objectKey, UUID stubRuleId) {
        SaveContext saveContext = new SaveContext();
        for (Violation violation : loadViolations(objectKey)) {
            saveContext.removing(violation);
        }
        List<CheckRun> runs = dataManager.load(CheckRun.class)
                .query("select e from mtr_CheckRun e where e.objectKey = :key")
                .parameter("key", objectKey)
                .list();
        for (CheckRun run : runs) {
            saveContext.removing(run);
        }
        if (stubRuleId != null) {
            dataManager.load(Rule.class).id(stubRuleId).optional().ifPresent(saveContext::removing);
        }
        dataManager.save(saveContext);
    }

    private static NoticeDto batteryNotice() {
        NoticeDto notice = new NoticeDto();
        notice.setId(21);
        notice.setNumber("0173100017099000001");
        notice.setSubject("Батареи");
        notice.setPublishedAt(LocalDateTime.of(2026, 2, 1, 10, 0));
        SpecPositionDto item = new SpecPositionDto();
        item.setId(21);
        item.setPos(1);
        item.setName("Батареи");
        item.setOkpd2("27.20.23.130");
        notice.setItems(List.of(item));
        return notice;
    }
}
