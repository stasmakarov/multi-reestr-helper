package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        assertThat(notice.getFinances()).hasSize(1);
        assertThat(notice.getFinances().get(0).getKbk()).isEqualTo("41703018820090049244");
        assertThat(notice.getFinances().get(0).getFinYear()).isEqualTo(2026);
        assertThat(notice.getFinances().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("43360.20"));
        assertThat(notice.getProtocols()).hasSize(2);
        assertThat(notice.getContract()).isNull();
        assertThat(notice.getWarnings()).isEmpty();
    }

    @Test
    void financesForNotice40() {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse("0173100017024000008"))
                .orElseThrow();

        assertThat(notice.getId()).isEqualTo(40);
        assertThat(notice.getFinances()).hasSize(1);
        assertThat(notice.getFinances().get(0).getKbk()).isEqualTo("41703018820090049242");
        assertThat(notice.getFinances().get(0).getFinYear()).isEqualTo(2025);
        assertThat(notice.getFinances().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("1744800.00"));
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

    @ParameterizedTest(name = "notice {0} -> {1} protocols")
    @CsvSource({
            "40, 0",
            "38, 0",
            "37, 2",
            "41, 0",
            "21, 1",
            "18, 2",
            "17, 2",
            "10, 2",
            "4, 2",
            "3, 2"
    })
    void protocolCountByNoticeId(int noticeId, int expectedCount) {
        NoticeDto notice = noticeCardService.loadNotice(noticeId).orElseThrow();
        assertThat(notice.getProtocols()).hasSize(expectedCount);
    }

    @Test
    void protocolsOfNotice3HaveTypeAndDate() {
        NoticeDto notice = noticeCardService.loadNotice(3).orElseThrow();

        assertThat(notice.getProtocols()).hasSize(2);

        ProtocolDto offers = notice.getProtocols().get(0);
        assertThat(offers.getId()).isEqualTo(73);
        assertThat(offers.getNumber()).isEqualTo("0173100017026000015-2");
        assertThat(offers.getDate()).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(offers.getTypeCode()).isEqualTo(0);
        assertThat(offers.getTypeName()).isEqualTo("тип 0 (нет в справочнике)");

        ProtocolDto summary = notice.getProtocols().get(1);
        assertThat(summary.getId()).isEqualTo(74);
        assertThat(summary.getNumber()).isEqualTo("0173100017026000015-3");
        assertThat(summary.getDate()).isNull();
        assertThat(summary.getTypeCode()).isEqualTo(12);
        assertThat(summary.getTypeName()).isEqualTo(
                "Протокол подведения итогов определения поставщика (подрядчика, исполнителя)"
        );
    }

    @Test
    void loadNoticeUnknownIdIsEmpty() {
        Optional<NoticeDto> found = noticeCardService.loadNotice(999999);
        assertThat(found).isEmpty();
    }
}
