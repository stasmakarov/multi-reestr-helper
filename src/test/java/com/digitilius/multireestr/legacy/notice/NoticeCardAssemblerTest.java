package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.NoticeFinanceDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeCardAssemblerTest {

    @Test
    void warningsForMissingBlocks() {
        NoticeDto notice = new NoticeDto();
        notice.setCustomer(null);

        List<String> warnings = NoticeCardAssembler.warnings(notice, 2);

        assertThat(warnings).containsExactly(
                "Нет заказчика",
                "Нет сведений о сроках процедуры",
                "Несколько блоков требований заказчика, показан первый",
                "Нет спецификации",
                "Нет данных о финансировании"
        );
    }

    @Test
    void procedureRowWithAllDatesNullWarnsAboutTerms() {
        NoticeDto notice = completeNotice();
        notice.setApplicationsFrom(null);
        notice.setApplicationsTo(null);
        notice.setBiddingDate(null);
        notice.setSummarizingDate(null);

        assertThat(NoticeCardAssembler.warnings(notice, 1))
                .containsExactly("Нет сведений о сроках процедуры");
    }

    @Test
    void emptyFinancesAddsWarning() {
        NoticeDto notice = completeNotice();
        notice.setFinances(List.of());

        assertThat(NoticeCardAssembler.warnings(notice, 1))
                .containsExactly("Нет данных о финансировании");
    }

    @Test
    void noWarningsWhenCardIsComplete() {
        assertThat(NoticeCardAssembler.warnings(completeNotice(), 1)).isEmpty();
    }

    private static NoticeDto completeNotice() {
        NoticeDto notice = new NoticeDto();
        notice.setCustomer(new OrganizationDto());
        notice.setApplicationsTo(LocalDateTime.of(2026, 4, 15, 10, 0));
        notice.setItems(List.of(new SpecPositionDto()));
        notice.setFinances(List.of(new NoticeFinanceDto()));
        return notice;
    }
}
