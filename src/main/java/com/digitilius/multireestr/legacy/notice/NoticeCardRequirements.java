package com.digitilius.multireestr.legacy.notice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NoticeCardRequirements(
        String ikz,
        String planPosition,
        BigDecimal appGuaranteeAmount,
        BigDecimal appGuaranteePct,
        BigDecimal contractGuaranteePct,
        Integer relativeLen,
        LocalDate executionEndDate
) {
}
