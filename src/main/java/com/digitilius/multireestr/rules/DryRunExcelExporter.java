package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public final class DryRunExcelExporter {

    private DryRunExcelExporter() {
    }

    public static byte[] writeBytes(List<CheckResultDto> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("dry-run");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("diff");
            header.createCell(1).setCellValue("objectKey");
            header.createCell(2).setCellValue("detail");
            header.createCell(3).setCellValue("message");
            header.createCell(4).setCellValue("severity");
            header.createCell(5).setCellValue("ruleCode");
            int rowNum = 1;
            if (rows != null) {
                for (CheckResultDto row : rows) {
                    Row excelRow = sheet.createRow(rowNum++);
                    excelRow.createCell(0).setCellValue(enumId(row.getDiff()));
                    excelRow.createCell(1).setCellValue(nullToEmpty(row.getObjectKey()));
                    excelRow.createCell(2).setCellValue(nullToEmpty(row.getDetail()));
                    excelRow.createCell(3).setCellValue(nullToEmpty(row.getMessage()));
                    excelRow.createCell(4).setCellValue(enumId(row.getSeverity()));
                    excelRow.createCell(5).setCellValue(nullToEmpty(row.getRuleCode()));
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String enumId(io.jmix.core.metamodel.datatype.EnumClass<?> value) {
        return value == null || value.getId() == null ? "" : String.valueOf(value.getId());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
