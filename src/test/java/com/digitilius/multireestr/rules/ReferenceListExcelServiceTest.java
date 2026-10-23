package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceListExcelServiceTest {

    private final ReferenceListExcelService service = new ReferenceListExcelService(null, null, null);

    @Test
    void parseSplitsCommaAndReadsFlags() throws Exception {
        byte[] bytes = workbook(new String[][]{
                {"value", "prefixMatch", "excluded", "groupCode", "caption", "validFrom", "validTo"},
                {"99.00, 99.01", "false", "false", "9.00", "два кода", "2024-01-01", ""},
                {"14.1", "true", "false", "1.00", "одежда", "01.01.2024", "31.12.2024"}
        });
        List<ReferenceListItem> items = service.parse(new ByteArrayInputStream(bytes));
        assertThat(items).extracting(ReferenceListItem::getValue)
                .containsExactly("99.00", "99.01", "14.1");
        assertThat(items.get(2).getPrefixMatch()).isTrue();
        assertThat(items.get(2).getValidFrom()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(items.get(2).getValidTo()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void exportRoundTrip() {
        ReferenceList list = new ReferenceList();
        list.setCode("PP1875_ANNEX1");
        ReferenceListItem item = new ReferenceListItem();
        item.setValue("27.20");
        item.setPrefixMatch(true);
        item.setExcluded(false);
        item.setGroupCode("3.00");
        list.setItems(List.of(item));
        byte[] bytes = service.export(list);
        List<ReferenceListItem> parsed = service.parse(new ByteArrayInputStream(bytes));
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0).getValue()).isEqualTo("27.20");
        assertThat(parsed.get(0).getPrefixMatch()).isTrue();
    }

    private static byte[] workbook(String[][] rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < rows[r].length; c++) {
                    row.createCell(c).setCellValue(rows[r][c]);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
