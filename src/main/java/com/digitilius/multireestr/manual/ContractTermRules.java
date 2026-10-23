package com.digitilius.multireestr.manual;

import com.digitilius.multireestr.entity.ContractTerm;
import com.digitilius.multireestr.entity.DeadlineSpec;

public final class ContractTermRules {

    private ContractTermRules() {
    }

    public static boolean isFormulaFilled(DeadlineSpec spec) {
        return spec != null
                && spec.getAnchor() != null
                && spec.getOffsetValue() != null
                && spec.getUnit() != null;
    }

    public static void validateXor(ContractTerm term) {
        boolean fixed = term.getFixedDate() != null;
        boolean formula = isFormulaFilled(term.getTerm());
        if (fixed && formula) {
            throw new IllegalArgumentException(
                    "Укажите либо фиксированную дату, либо формулу срока, не оба сразу");
        }
        if (!fixed && !formula) {
            throw new IllegalArgumentException(
                    "Укажите фиксированную дату или формулу срока (якорь, смещение, единица)");
        }
    }
}
