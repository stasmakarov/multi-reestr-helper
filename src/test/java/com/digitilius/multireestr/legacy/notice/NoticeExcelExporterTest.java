package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.LotDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeExcelExporterTest {

    private final NoticeExcelExporter exporter = new NoticeExcelExporter();

    @Test
    void writesSheetNamesHeadersAndRowCounts() throws IOException {
        byte[] bytes = exporter.writeBytes(List.of(noticeWithTwoItems()));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            Sheet notices = workbook.getSheet(NoticeExcelExporter.SHEET_NOTICES);
            Sheet spec = workbook.getSheet(NoticeExcelExporter.SHEET_SPECIFICATIONS);
            assertThat(notices).isNotNull();
            assertThat(spec).isNotNull();
            assertThat(headerValues(notices)).containsExactly(NoticeExcelExporter.NOTICE_HEADERS);
            assertThat(headerValues(spec)).containsExactly(NoticeExcelExporter.SPEC_HEADERS);
            assertThat(headerValues(notices)).containsExactly(
                    "Номер извещения",
                    "Дата публикации",
                    "Объект закупки",
                    "Способ",
                    "НМЦК, руб.",
                    "Заказчик",
                    "Лотов",
                    "Позиций",
                    "Окончание подачи заявок",
                    "Протоколы",
                    "Победитель / поставщик",
                    "№ контракта",
                    "Дата контракта",
                    "Цена контракта, руб.",
                    "Снижение, %",
                    "Оплачено, руб.",
                    "Документов приёмки",
                    "Примечания"
            );
            assertThat(headerValues(spec)).containsExactly(
                    "Номер извещения",
                    "Дата",
                    "№ п/п",
                    "Наименование",
                    "ОКПД2",
                    "КТРУ",
                    "Кол-во",
                    "Ед.",
                    "Цена, руб.",
                    "Сумма, руб.",
                    "Характеристики КТРУ"
            );
            assertThat(notices.getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(spec.getPhysicalNumberOfRows()).isEqualTo(3);
        }
    }

    @Test
    void emptyListWritesHeadersOnly() throws IOException {
        byte[] bytes = exporter.writeBytes(List.of());

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet notices = workbook.getSheetAt(0);
            Sheet spec = workbook.getSheetAt(1);
            assertThat(notices.getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(spec.getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(headerValues(notices)).containsExactly(NoticeExcelExporter.NOTICE_HEADERS);
            assertFreezeAndFilter(notices, 18);
            assertFreezeAndFilter(spec, 11);
        }
    }

    @Test
    void writesExcelDatesMoneyPercentAndNotes() throws IOException {
        NoticeDto notice = noticeWithTwoItems();
        notice.setPublishedAt(LocalDateTime.of(2026, 4, 7, 16, 57, 55));
        notice.setApplicationsTo(LocalDateTime.of(2026, 4, 15, 10, 0, 0));
        notice.setWarnings(List.of("есть дубль", "участники не загружены"));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(exporter.writeBytes(List.of(notice))))) {
            Row row = workbook.getSheetAt(0).getRow(1);
            Cell published = row.getCell(1);
            assertThat(DateUtil.isCellDateFormatted(published)).isTrue();
            assertThat(published.getLocalDateTimeCellValue()).isEqualTo(LocalDateTime.of(2026, 4, 7, 16, 57));
            assertThat(published.getCellStyle().getDataFormatString()).isEqualTo("dd.MM.yyyy HH:mm");

            Cell nmck = row.getCell(4);
            assertThat(nmck.getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(nmck.getNumericCellValue()).isEqualTo(43360.20);
            assertThat(nmck.getCellStyle().getDataFormatString()).isEqualTo("#,##0.00");

            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Электронный аукцион");
            assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(1);
            assertThat(row.getCell(7).getNumericCellValue()).isEqualTo(2);
            assertThat(row.getCell(9).getStringCellValue()).isEqualTo("0173100017026000015-2, 0173100017026000015-3");

            Cell contractDate = row.getCell(12);
            assertThat(DateUtil.isCellDateFormatted(contractDate)).isTrue();
            assertThat(contractDate.getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 4, 27));
            assertThat(contractDate.getCellStyle().getDataFormatString()).isEqualTo("dd.MM.yyyy");

            Cell reduction = row.getCell(14);
            assertThat(reduction.getNumericCellValue()).isEqualTo(63.1);
            assertThat(reduction.getCellStyle().getDataFormatString()).isEqualTo("0.0");

            assertThat(row.getCell(16).getNumericCellValue()).isEqualTo(1);
            assertThat(row.getCell(17).getStringCellValue()).isEqualTo("есть дубль; участники не загружены");

            Row spec = workbook.getSheetAt(1).getRow(1);
            assertThat(spec.getCell(6).getNumericCellValue()).isEqualTo(3060);
            assertThat(spec.getCell(8).getCellStyle().getDataFormatString()).isEqualTo("#,##0.00");
            assertThat(spec.getCell(8).getNumericCellValue()).isEqualTo(14.17);
        }
    }

    @Test
    void noContractLeavesContractColumnsBlankAndAddsNote() throws IOException {
        NoticeDto notice = new NoticeDto();
        notice.setNumber("0173100017024000010");
        notice.setMethodName("Электронный аукцион");
        notice.setLots(List.of());
        notice.setItems(List.of());
        notice.setContract(null);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(exporter.writeBytes(List.of(notice))))) {
            Row row = workbook.getSheetAt(0).getRow(1);
            for (int col = 10; col <= 16; col++) {
                assertThat(row.getCell(col)).isNull();
            }
            assertThat(row.getCell(17).getStringCellValue()).isEqualTo("Контракт не найден");
        }
    }

    @Test
    void freezeAutoFilterAndColumnWidthBounds() throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(exporter.writeBytes(List.of(noticeWithTwoItems()))))) {
            Sheet notices = workbook.getSheetAt(0);
            assertFreezeAndFilter(notices, 18);
            assertFreezeAndFilter(workbook.getSheetAt(1), 11);
            for (int col = 0; col < 18; col++) {
                int chars = notices.getColumnWidth(col) / 256;
                assertThat(chars).isBetween(10, 60);
            }
            Cell header = notices.getRow(0).getCell(0);
            assertThat(header.getCellStyle().getWrapText()).isTrue();
            assertThat(workbook.getFontAt(header.getCellStyle().getFontIndex()).getBold()).isTrue();
        }
    }

    private static void assertFreezeAndFilter(Sheet sheet, int columns) {
        PaneInformation pane = sheet.getPaneInformation();
        assertThat(pane).isNotNull();
        assertThat(pane.isFreezePane()).isTrue();
        assertThat(pane.getHorizontalSplitTopRow()).isEqualTo((short) 1);
        XSSFSheet xssf = (XSSFSheet) sheet;
        assertThat(xssf.getCTWorksheet().isSetAutoFilter()).isTrue();
        assertThat(xssf.getCTWorksheet().getAutoFilter().getRef()).isEqualTo(
                "A1:" + org.apache.poi.ss.util.CellReference.convertNumToColString(columns - 1)
                        + Math.max(sheet.getPhysicalNumberOfRows(), 1)
        );
    }

    private static String[] headerValues(Sheet sheet) {
        Row header = sheet.getRow(0);
        String[] values = new String[header.getLastCellNum()];
        for (int i = 0; i < values.length; i++) {
            values[i] = header.getCell(i).getStringCellValue();
        }
        return values;
    }

    private static NoticeDto noticeWithTwoItems() {
        OrganizationDto customer = new OrganizationDto();
        customer.setName("МСК");
        OrganizationDto supplier = new OrganizationDto();
        supplier.setName("SUPPLIER");
        supplier.setInn("000000000258");

        ContractDto contract = new ContractDto();
        contract.setNumber("0173100017026000015");
        contract.setDate(LocalDate.of(2026, 4, 27));
        contract.setPrice(new BigDecimal("16000"));
        contract.setReductionPct(new BigDecimal("63.1"));
        contract.setPaidTotal(new BigDecimal("16000"));
        contract.setSupplier(supplier);
        ExecutionDto acceptance = new ExecutionDto();
        acceptance.setKind(ExecutionKind.ACCEPTANCE);
        ExecutionDto payment = new ExecutionDto();
        payment.setKind(ExecutionKind.PAYMENT);
        contract.setExecutions(List.of(acceptance, payment));

        ProtocolDto first = new ProtocolDto();
        first.setNumber("0173100017026000015-2");
        ProtocolDto second = new ProtocolDto();
        second.setNumber("0173100017026000015-3");

        SpecPositionDto item1 = new SpecPositionDto();
        item1.setPos(1);
        item1.setName("Папка картонная");
        item1.setOkpd2("17.23.13.193");
        item1.setKtru("17.23.13.193-00000002");
        item1.setQty(new BigDecimal("3060"));
        item1.setUnit("Штука");
        item1.setPrice(new BigDecimal("14.17"));
        item1.setAmount(new BigDecimal("43360.20"));
        item1.setCharacteristicsText("Плотность картона, г/м2: >= 220 < 260");
        SpecPositionDto item2 = new SpecPositionDto();
        item2.setPos(2);
        item2.setName("Другая позиция");

        LotDto lot = new LotDto();
        lot.setNumber(1);

        NoticeDto notice = new NoticeDto();
        notice.setNumber("0173100017026000015");
        notice.setPublishedAt(LocalDateTime.of(2026, 4, 7, 16, 57));
        notice.setSubject("Папки");
        notice.setMethodName("Электронный аукцион");
        notice.setNmck(new BigDecimal("43360.20"));
        notice.setCustomer(customer);
        notice.setLots(List.of(lot));
        notice.setItems(List.of(item1, item2));
        notice.setProtocols(List.of(first, second));
        notice.setContract(contract);
        return notice;
    }
}
