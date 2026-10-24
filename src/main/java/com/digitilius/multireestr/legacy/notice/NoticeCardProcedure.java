package com.digitilius.multireestr.legacy.notice;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoticeCardProcedure(
        LocalDateTime applicationsFrom,
        LocalDateTime applicationsTo,
        LocalDate biddingDate,
        LocalDate summarizingDate
) {
}
