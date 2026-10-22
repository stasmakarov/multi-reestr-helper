package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class NoticeCardServiceTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    NoticeCardService noticeCardService;

    @Test
    void loadOriginalForGoldenNumber() {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse("0173100017026000015"))
                .orElseThrow();

        assertThat(notice.getId()).isEqualTo(3);
        assertThat(notice.getCustomer()).isNotNull();
        assertThat(notice.getCustomer().getInn()).isEqualTo("7714841940");
        assertThat(notice.getNmck()).isEqualByComparingTo(new BigDecimal("43360.20"));
        assertThat(notice.getSubject()).isEqualTo(
                "Канцелярские принадлежности папки «Дело» без скоросшивателя, картонные"
        );
        assertThat(notice.getApplicationsTo()).isEqualTo(LocalDateTime.of(2026, 4, 15, 10, 0));
        assertThat(notice.getItems()).isNotEmpty();
        assertThat(notice.getLaw()).isEqualTo("44-ФЗ");
        assertThat(notice.getMethodName()).isEqualTo("Электронный аукцион");
        assertThat(notice.getPreferences()).contains("ET44");
        assertThat(notice.getProtocols()).isEmpty();
        assertThat(notice.getContract()).isNull();
        assertThat(notice.getWarnings()).isEmpty();
    }

    @Test
    void ktruPositionsHaveCodeAndCharacteristics() {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse("0173100017026000015"))
                .orElseThrow();

        assertThat(notice.getItems())
                .filteredOn(item -> item.getKtru() != null)
                .isNotEmpty()
                .allSatisfy(item -> {
                    assertThat(item.getKtru()).matches("\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{3}-\\d{8}");
                    assertThat(item.getCharacteristics()).isNotEmpty();
                });

        SpecPositionDto folder = notice.getItems().get(0);
        assertThat(folder.getKtru()).isEqualTo("17.23.13.193-00000002");
        assertThat(folder.getCharacteristicsText()).contains("Плотность картона, г/м2: >= 220 < 260");
    }

    @Test
    void loadNoticeUnknownIdIsEmpty() {
        Optional<NoticeDto> found = noticeCardService.loadNotice(999999);
        assertThat(found).isEmpty();
    }
}
