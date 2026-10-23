package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.ContractTermType;
import com.digitilius.multireestr.entity.DayCountUnit;
import com.digitilius.multireestr.entity.DeadlineAnchor;
import com.digitilius.multireestr.entity.DeadlineSpec;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractTermRulesTest {

    @Test
    void bothFixedDateAndFormulaAreRejected() {
        ContractTerm term = term();
        term.setFixedDate(LocalDate.of(2026, 1, 22));
        term.setTerm(formula());

        assertThatThrownBy(() -> ContractTermRules.validateXor(term))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не оба сразу");
    }

    @Test
    void neitherFixedDateNorFormulaIsRejected() {
        assertThatThrownBy(() -> ContractTermRules.validateXor(term()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("фиксированную дату или формулу");
    }

    private static ContractTerm term() {
        ContractTerm term = new ContractTerm();
        term.setContractLegacyId(18);
        term.setTermType(ContractTermType.DELIVERY);
        return term;
    }

    private static DeadlineSpec formula() {
        DeadlineSpec spec = new DeadlineSpec();
        spec.setAnchor(DeadlineAnchor.CONTRACT_SIGNED);
        spec.setOffsetValue(30);
        spec.setUnit(DayCountUnit.CALENDAR_DAYS);
        return spec;
    }
}
