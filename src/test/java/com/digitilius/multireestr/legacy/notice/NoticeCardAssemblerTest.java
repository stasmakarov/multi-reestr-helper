package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeCardAssemblerTest {

    @Test
    void warningsForMissingBlocks() {
        NoticeDto notice = new NoticeDto();
        notice.setCustomer(null);

        List<String> warnings = NoticeCardAssembler.warnings(notice, false, 2, false);

        assertThat(warnings).containsExactly(
                "Нет заказчика",
                "Нет сведений о сроках процедуры",
                "Несколько блоков требований заказчика, показан первый",
                "Нет спецификации"
        );
    }

    @Test
    void noWarningsWhenCardIsComplete() {
        NoticeDto notice = new NoticeDto();
        notice.setCustomer(new OrganizationDto());

        assertThat(NoticeCardAssembler.warnings(notice, true, 1, true)).isEmpty();
    }
}
