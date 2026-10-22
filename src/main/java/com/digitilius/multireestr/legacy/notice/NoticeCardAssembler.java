package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;

import java.util.ArrayList;
import java.util.List;

final class NoticeCardAssembler {

    private NoticeCardAssembler() {
    }

    static List<String> warnings(NoticeDto notice, int requirementCount) {
        List<String> warnings = new ArrayList<>();
        if (notice.getCustomer() == null) {
            warnings.add("Нет заказчика");
        }
        if (!hasProcedure(notice)) {
            warnings.add("Нет сведений о сроках процедуры");
        }
        if (requirementCount > 1) {
            warnings.add("Несколько блоков требований заказчика, показан первый");
        }
        if (notice.getItems() == null || notice.getItems().isEmpty()) {
            warnings.add("Нет спецификации");
        }
        if (notice.getFinances() == null || notice.getFinances().isEmpty()) {
            warnings.add("Нет данных о финансировании");
        }
        return warnings;
    }

    private static boolean hasProcedure(NoticeDto notice) {
        return notice.getApplicationsFrom() != null
                || notice.getApplicationsTo() != null
                || notice.getBiddingDate() != null
                || notice.getSummarizingDate() != null;
    }
}
