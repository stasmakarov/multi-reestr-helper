package com.digitilius.multireestr.scan;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.Violation;
import com.digitilius.multireestr.entity.ViolationStatus;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.rules.OnDemandCheckService;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@RequireDocker
@TestPropertySource(properties = {
        "mtr.scan.max-notices=0",
        "mtr.scan.notice-lookback-days=0"
})
class ScheduledCheckServiceLegacyTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    ScheduledCheckService scheduledCheckService;
    @Autowired
    OnDemandCheckService onDemandCheckService;
    @Autowired
    NewViolationCountService newViolationCountService;
    @Autowired
    DataManager dataManager;

    @Test
    void scheduledScanWritesDeliveryHitAndKeepsFalsePositive() {
        int overdueId = 99018;
        ContractTerm overdue = overdueTerm(overdueId);
        CheckRun first = null;
        CheckRun second = null;
        try {
            int newBefore = newViolationCountService.countNew();
            first = scheduledCheckService.runScheduled();
            assertThat(first.getMode()).isEqualTo(CheckRunMode.SCHEDULED);
            assertThat(first.getStatus()).isEqualTo(CheckRunStatus.SUCCESS);
            assertThat(first.getObjectsChecked()).isGreaterThanOrEqualTo(1);

            List<Violation> overdueHits = loadViolations(String.valueOf(overdueId));
            assertThat(overdueHits).hasSize(1);
            Violation hit = overdueHits.get(0);
            assertThat(hit.getStatus()).isEqualTo(ViolationStatus.NEW);
            assertThat(hit.getRule().getCode()).isEqualTo(ContractTermDeadlineScan.R09_DELIVERY);
            assertThat(hit.getDueDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(hit.getFingerprint()).hasSize(64);
            assertThat(newViolationCountService.countNew()).isGreaterThan(newBefore);

            hit.setComment("не применяется");
            Violation falsePositive = onDemandCheckService.markFalsePositive(hit);
            assertThat(falsePositive.getStatus()).isEqualTo(ViolationStatus.FALSE_POSITIVE);

            second = scheduledCheckService.runScheduled();
            assertThat(dataManager.load(Violation.class).id(falsePositive.getId()).one().getStatus())
                    .isEqualTo(ViolationStatus.FALSE_POSITIVE);
            assertThat(loadViolations(String.valueOf(overdueId))).hasSize(1);
        } finally {
            removeByObjectKeys(List.of(String.valueOf(overdueId)));
            if (first != null) {
                dataManager.remove(first);
            }
            if (second != null) {
                dataManager.remove(second);
            }
            dataManager.remove(overdue);
        }
    }

    private ContractTerm overdueTerm(int legacyId) {
        ContractTerm term = dataManager.create(ContractTerm.class);
        term.setContractLegacyId(legacyId);
        term.setTermType(ContractTermType.DELIVERY);
        term.setFixedDate(LocalDate.of(2026, 1, 1));
        return dataManager.save(term);
    }

    private List<Violation> loadViolations(String objectKey) {
        return dataManager.load(Violation.class)
                .query("select e from mtr_Violation e join fetch e.rule where e.objectKey = :key")
                .parameter("key", objectKey)
                .list();
    }

    private void removeByObjectKeys(List<String> keys) {
        SaveContext saveContext = new SaveContext();
        for (String key : keys) {
            for (Violation violation : loadViolations(key)) {
                saveContext.removing(violation);
            }
        }
        dataManager.save(saveContext);
    }
}
