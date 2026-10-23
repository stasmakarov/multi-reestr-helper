package com.digitilius.multireestr.scan;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.DeadlineSpec;
import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.Severity;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class ContractTermDeadlineScanTest {

    @Autowired
    ContractTermDeadlineScan scan;
    @Autowired
    DataManager dataManager;

    @Test
    void overdueFixedDateWithoutFactIsError() {
        LocalDate today = LocalDate.of(2026, 6, 1);
        Bound r09 = load(ContractTermDeadlineScan.R09_DELIVERY);
        ContractTerm term = delivery(9011);
        term.setFixedDate(today.minusDays(5));

        ContractTermDeadlineScan.Assessment assessment =
                scan.assess(term, null, null, null, r09.rule(), r09.version(), today);

        assertThat(assessment.evaluated()).isTrue();
        assertThat(assessment.hit()).isNotNull();
        assertThat(assessment.hit().getSeverity()).isEqualTo(Severity.ERROR);
        assertThat(assessment.hit().getFingerprint()).hasSize(64);
        assertThat(assessment.dueDate()).isEqualTo(today.minusDays(5));
    }

    @Test
    void approachingDueDateIsWarning() {
        LocalDate today = LocalDate.of(2026, 6, 1);
        Bound r09 = load(ContractTermDeadlineScan.R09_DELIVERY);
        ContractTerm term = delivery(9012);
        term.setFixedDate(today.plusDays(3));
        DeadlineSpec spec = new DeadlineSpec();
        spec.setWarnBeforeDays(7);
        term.setTerm(spec);

        ContractTermDeadlineScan.Assessment assessment =
                scan.assess(term, null, null, null, r09.rule(), r09.version(), today);

        assertThat(assessment.evaluated()).isTrue();
        assertThat(assessment.hit()).isNotNull();
        assertThat(assessment.hit().getSeverity()).isEqualTo(Severity.WARNING);
        assertThat(assessment.hit().getDetail()).isEqualTo(ContractTermDeadlineScan.DETAIL_DELIVERY);
    }

    @Test
    void missingDueDateIsNotEvaluated() {
        Bound r09 = load(ContractTermDeadlineScan.R09_DELIVERY);
        ContractTerm term = delivery(9013);
        DeadlineSpec spec = new DeadlineSpec();
        spec.setAnchor(DeadlineAnchor.CONTRACT_SIGNED);
        spec.setOffsetValue(30);
        spec.setUnit(DayCountUnit.CALENDAR_DAYS);
        term.setTerm(spec);

        ContractTermDeadlineScan.Assessment assessment =
                scan.assess(term, null, null, null, r09.rule(), r09.version(), LocalDate.of(2026, 6, 1));

        assertThat(assessment.evaluated()).isFalse();
        assertThat(assessment.hit()).isNull();
    }

    @Test
    void unloadedCalendarYearThrows() {
        Bound r09 = load(ContractTermDeadlineScan.R09_DELIVERY);
        ContractTerm term = delivery(9014);
        DeadlineSpec spec = new DeadlineSpec();
        spec.setAnchor(DeadlineAnchor.CONTRACT_SIGNED);
        spec.setOffsetValue(10);
        spec.setUnit(DayCountUnit.WORKING_DAYS);
        term.setTerm(spec);
        ContractDto contract = new ContractDto();
        contract.setId(9014);
        contract.setDate(LocalDate.of(1999, 1, 15));

        assertThatThrownBy(() ->
                scan.assess(term, null, contract, null, r09.rule(), r09.version(), LocalDate.of(2026, 6, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("не загружен в производственный календарь");
    }

    private Bound load(String code) {
        Rule rule = dataManager.load(Rule.class)
                .query("select e from mtr_Rule e where e.code = :code")
                .parameter("code", code)
                .fetchPlan(builder -> builder.addFetchPlan(FetchPlan.BASE).add("versions", FetchPlan.BASE))
                .one();
        RuleVersion version = rule.getVersions().get(0);
        return new Bound(rule, version);
    }

    private static ContractTerm delivery(int legacyId) {
        ContractTerm term = new ContractTerm();
        term.setContractLegacyId(legacyId);
        term.setTermType(ContractTermType.DELIVERY);
        return term;
    }

    private record Bound(Rule rule, RuleVersion version) {
    }
}
