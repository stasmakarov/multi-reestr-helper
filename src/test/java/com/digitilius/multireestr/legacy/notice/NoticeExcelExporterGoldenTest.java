package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class NoticeExcelExporterGoldenTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    NoticeCardService noticeCardService;
    @Autowired
    NoticeExcelExporter noticeExcelExporter;

    @Test
    void exportNoticeId3MatchesEtalonValues() throws IOException {
        NoticeDto notice = noticeCardService.loadNotice(3).orElseThrow();
        byte[] bytes = noticeExcelExporter.writeBytes(java.util.List.of(notice));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet notices = workbook.getSheet(NoticeExcelExporter.SHEET_NOTICES);
            Sheet spec = workbook.getSheet(NoticeExcelExporter.SHEET_SPECIFICATIONS);
            assertThat(notices.getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(spec.getPhysicalNumberOfRows()).isEqualTo(2);

            Row row = notices.getRow(1);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("0173100017026000015");
            assertThat(row.getCell(1).getLocalDateTimeCellValue()).isEqualTo(LocalDateTime.of(2026, 4, 7, 16, 57));
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo(
                    "Канцелярские принадлежности папки «Дело» без скоросшивателя, картонные"
            );
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Электронный аукцион");
            assertThat(row.getCell(4).getNumericCellValue()).isEqualTo(43360.20);
            assertThat(row.getCell(5).getStringCellValue()).isEqualTo("МСК");
            assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(1);
            assertThat(row.getCell(7).getNumericCellValue()).isEqualTo(1);
            assertThat(row.getCell(8).getLocalDateTimeCellValue()).isEqualTo(LocalDateTime.of(2026, 4, 15, 10, 0));
            assertThat(row.getCell(9).getStringCellValue()).isEqualTo("0173100017026000015-2, 0173100017026000015-3");
            assertThat(row.getCell(10).getStringCellValue()).contains("(ИНН 332711778952)");
            assertThat(row.getCell(11).getStringCellValue()).isEqualTo("0173100017026000015");
            assertThat(row.getCell(12).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 4, 27));
            assertThat(row.getCell(13).getNumericCellValue()).isEqualTo(16000);
            assertThat(row.getCell(14).getNumericCellValue()).isEqualTo(63.1);
            assertThat(row.getCell(15).getNumericCellValue()).isEqualTo(16000);
            assertThat(row.getCell(16).getNumericCellValue()).isEqualTo(1);
            assertThat(blank(row.getCell(17))).isTrue();

            Row specRow = spec.getRow(1);
            assertThat(specRow.getCell(0).getStringCellValue()).isEqualTo("0173100017026000015");
            assertThat(specRow.getCell(1).getLocalDateTimeCellValue()).isEqualTo(LocalDateTime.of(2026, 4, 7, 16, 57));
            assertThat(specRow.getCell(2).getNumericCellValue()).isEqualTo(1);
            assertThat(specRow.getCell(3).getStringCellValue()).isEqualTo("Папка картонная");
            assertThat(specRow.getCell(4).getStringCellValue()).isEqualTo("17.23.13.193");
            assertThat(specRow.getCell(5).getStringCellValue()).isEqualTo("17.23.13.193-00000002");
            assertThat(specRow.getCell(6).getNumericCellValue()).isEqualTo(3060);
            assertThat(specRow.getCell(7).getStringCellValue()).isEqualTo("Штука");
            assertThat(specRow.getCell(8).getNumericCellValue()).isEqualTo(14.17);
            assertThat(specRow.getCell(9).getNumericCellValue()).isEqualTo(43360.20);
            assertThat(specRow.getCell(10).getStringCellValue()).isEqualTo(
                    "Плотность картона, г/м2: >= 220 < 260; Тип: Папка-обложка без скоросшивателя; Формат: A4; Цвет: белый; Ширина корешка: 25"
            );
        }
    }

    @Test
    void exportNoticeId38HasEmptyContractColumnsAndMissingContractNote() throws IOException {
        NoticeDto notice = noticeCardService.loadNotice(38).orElseThrow();
        assertThat(notice.getContract()).isNull();
        byte[] bytes = noticeExcelExporter.writeBytes(java.util.List.of(notice));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Row row = workbook.getSheet(NoticeExcelExporter.SHEET_NOTICES).getRow(1);
            for (int col = 10; col <= 16; col++) {
                assertThat(blank(row.getCell(col))).isTrue();
            }
            assertThat(row.getCell(17).getStringCellValue()).contains("Контракт не найден");
        }
    }

    private static boolean blank(Cell cell) {
        return cell == null || cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK;
    }
}
