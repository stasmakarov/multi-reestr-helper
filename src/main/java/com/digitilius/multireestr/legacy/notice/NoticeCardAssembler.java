package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;

import java.util.ArrayList;
import java.util.List;

final class NoticeCardAssembler {

    private NoticeCardAssembler() {
    }

    static List<String> warnings(
            NoticeDto notice,
            boolean hasProcedure,
            int requirementCount,
            boolean hasSpec
    ) {
        List<String> warnings = new ArrayList<>();
        if (notice.getCustomer() == null) {
            warnings.add("Нет заказчика");
        }
        if (!hasProcedure) {
            warnings.add("Нет сведений о сроках процедуры");
        }
        if (requirementCount > 1) {
            warnings.add("Несколько блоков требований заказчика, показан первый");
        }
        if (!hasSpec) {
            warnings.add("Нет спецификации");
        }
        return warnings;
    }
}
