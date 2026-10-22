package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ContractItemDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
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

        ContractDto contract = notice.getContract();
        assertThat(contract).isNotNull();
        assertThat(contract.getId()).isEqualTo(30);
        assertThat(contract.getNumber()).isEqualTo("0173100017026000015");
        assertThat(contract.getDate()).isEqualTo(LocalDate.of(2026, 4, 27));
        assertThat(contract.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 27));
        assertThat(contract.getPrice()).isEqualByComparingTo("16000");
        assertThat(contract.getReductionPct()).isEqualByComparingTo("63.1");
        assertThat(contract.getRegistryNumber()).isEqualTo("1771484194026000013");
        assertThat(contract.getSupplier()).isNotNull();
        assertThat(contract.getSupplier().getInn()).isEqualTo("000000000258");
        assertThat(contract.getPaidTotal()).isEqualByComparingTo("16000");
        assertThat(contract.getExecutions()).hasSize(2);
        assertThat(contract.getItems()).hasSize(1);
        ContractItemDto folder = contract.getItems().get(0);
        assertThat(folder.getName()).isEqualTo("Папка картонная. Товарный знак: отсутствует");
        assertThat(folder.getOkpd2()).isEqualTo("17.23.13.193");
        assertThat(folder.getKtru()).isEqualTo("17.23.13.193-00000002");
        assertThat(folder.getQty()).isEqualByComparingTo("3060");
        assertThat(folder.getUnit()).isEqualTo("Штука");
        assertThat(folder.getPrice()).isEqualByComparingTo("5.22875816993");
        assertThat(folder.getAmount()).isEqualByComparingTo("16000");
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

    @ParameterizedTest(name = "notice {0} -> contract {1}")
    @CsvSource(nullValues = "-", value = {
            "40, 3, 1744800, 0.0, 7708826943, 2",
            "38, -, -, -, -, -",
            "37, 8, 445200, 20.5, 000000000184, 1",
            "41, -, -, -, -, -",
            "21, 15, 252700, 0, 9701105424, 8",
            "18, 18, 800000, 0, 000000000321, 1",
            "17, 19, 347200, 38.0, 5526006170, 1",
            "10, 26, 93050, 46.8, 7721129652, 2",
            "4, 29, 33000, 0, 3662046958, 2",
            "3, 30, 16000, 63.1, 000000000258, 1"
    })
    void contractByNoticeId(
            int noticeId,
            Integer contractId,
            String price,
            String reductionPct,
            String supplierInn,
            Integer itemCount
    ) {
        NoticeDto notice = noticeCardService.loadNotice(noticeId).orElseThrow();
        if (contractId == null) {
            assertThat(notice.getContract()).isNull();
            return;
        }
        ContractDto contract = notice.getContract();
        assertThat(contract).isNotNull();
        assertThat(contract.getId()).isEqualTo(contractId);
        assertThat(contract.getPrice()).isEqualByComparingTo(price);
        assertThat(contract.getReductionPct()).isEqualByComparingTo(reductionPct);
        assertThat(contract.getSupplier()).isNotNull();
        assertThat(contract.getSupplier().getInn()).isEqualTo(supplierInn);
        assertThat(contract.getItems()).hasSize(itemCount);
    }

    @ParameterizedTest(name = "notice {0} -> {1} acceptances, paid {2}")
    @CsvSource(nullValues = "-", value = {
            "40, 12, 1744800",
            "38, -, -",
            "37, 1, 445200",
            "41, -, -",
            "21, 1, 252700",
            "18, 1, 800000",
            "17, 2, 347200",
            "10, 1, 93050",
            "4, 1, 33000",
            "3, 1, 16000"
    })
    void executionsByNoticeId(int noticeId, Integer acceptanceCount, String paidTotal) {
        NoticeDto notice = noticeCardService.loadNotice(noticeId).orElseThrow();
        if (acceptanceCount == null) {
            assertThat(notice.getContract()).isNull();
            return;
        }
        ContractDto contract = notice.getContract();
        assertThat(contract).isNotNull();
        assertThat(contract.getExecutions()).isNotNull();
        assertThat(contract.getExecutions())
                .filteredOn(execution -> execution.getKind() == ExecutionKind.ACCEPTANCE)
                .hasSize(acceptanceCount);
        assertThat(contract.getPaidTotal()).isEqualByComparingTo(paidTotal);
    }

    @Test
    void executionsOfNotice3HaveAcceptanceAndPayment() {
        NoticeDto notice = noticeCardService.loadNotice(3).orElseThrow();
        ContractDto contract = notice.getContract();
        assertThat(contract.getPaidTotal()).isEqualByComparingTo("16000");
        assertThat(contract.getExecutions()).hasSize(2);

        ExecutionDto acceptance = contract.getExecutions().get(0);
        assertThat(acceptance.getId()).isEqualTo(98);
        assertThat(acceptance.getKind()).isEqualTo(ExecutionKind.ACCEPTANCE);
        assertThat(acceptance.getDocNumber()).isEqualTo("ЯЕ26089");
        assertThat(acceptance.getDocDate()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(acceptance.getAcceptDate()).isEqualTo(LocalDate.of(2026, 5, 12));
        assertThat(acceptance.getAmount()).isEqualByComparingTo("16000");
        assertThat(acceptance.getStageNumber()).isEqualTo(1);

        ExecutionDto payment = contract.getExecutions().get(1);
        assertThat(payment.getId()).isEqualTo(97);
        assertThat(payment.getKind()).isEqualTo(ExecutionKind.PAYMENT);
        assertThat(payment.getDocName()).isEqualTo("Платежное поручение");
        assertThat(payment.getDocNumber()).isEqualTo("8202");
        assertThat(payment.getDocDate()).isEqualTo(LocalDate.of(2026, 5, 13));
        assertThat(payment.getAmount()).isEqualByComparingTo("16000");
        assertThat(payment.getStageNumber()).isEqualTo(2);
    }

    @Test
    void executionsOfNotice17KeepZeroAcceptanceAndPaidTotal() {
        NoticeDto notice = noticeCardService.loadNotice(17).orElseThrow();
        ContractDto contract = notice.getContract();
        assertThat(contract.getPaidTotal()).isEqualByComparingTo("347200");
        assertThat(contract.getExecutions()).hasSize(3);
        assertThat(contract.getExecutions())
                .filteredOn(execution -> execution.getKind() == ExecutionKind.ACCEPTANCE)
                .hasSize(2);

        ExecutionDto zeroAcceptance = contract.getExecutions().get(0);
        assertThat(zeroAcceptance.getId()).isEqualTo(76);
        assertThat(zeroAcceptance.getKind()).isEqualTo(ExecutionKind.ACCEPTANCE);
        assertThat(zeroAcceptance.getDocNumber()).isEqualTo("945/ОМТО-6713/26");
        assertThat(zeroAcceptance.getAmount()).isEqualByComparingTo("0.00");
        assertThat(zeroAcceptance.getImproperExecution()).contains("пени");
    }

    @Test
    void executionsOfNotice40SeparateAcceptancesFromPayments() {
        NoticeDto notice = noticeCardService.loadNotice(40).orElseThrow();
        ContractDto contract = notice.getContract();
        assertThat(contract.getPaidTotal()).isEqualByComparingTo("1744800");
        assertThat(contract.getExecutions()).hasSize(25);
        assertThat(contract.getExecutions())
                .filteredOn(execution -> execution.getKind() == ExecutionKind.ACCEPTANCE)
                .hasSize(12)
                .anySatisfy(execution -> {
                    assertThat(execution.getDocNumber()).isEqualTo("945/ОМТО-18605/25");
                    assertThat(execution.getAmount()).isEqualByComparingTo("0.00");
                });
        assertThat(contract.getExecutions())
                .filteredOn(execution -> execution.getKind() == ExecutionKind.PAYMENT)
                .hasSize(13);
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
