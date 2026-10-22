package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoticeExcelExporter {

    public static final String SHEET_NOTICES = "Извещения";
    public static final String SHEET_SPECIFICATIONS = "Спецификации";
    public static final int MAX_NOTICES = 20;
    public static final String NO_CONTRACT_NOTE = "Контракт не найден";

    public static final String[] NOTICE_HEADERS = {
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
    };

    public static final String[] SPEC_HEADERS = {
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
    };

    private static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String MONEY_FORMAT = "#,##0.00";
    private static final String PERCENT_FORMAT = "0.0";

    public byte[] writeBytes(List<NoticeDto> notices) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(notices, out);
        return out.toByteArray();
    }

    public void write(List<NoticeDto> notices, OutputStream out) {
        List<NoticeDto> rows = notices == null ? List.of() : notices;
        try (Workbook workbook = new XSSFWorkbook()) {
            Styles styles = Styles.create(workbook);
            Sheet noticesSheet = workbook.createSheet(SHEET_NOTICES);
            Sheet specSheet = workbook.createSheet(SHEET_SPECIFICATIONS);
            writeNotices(noticesSheet, rows, styles);
            writeSpecifications(specSheet, rows, styles);
            workbook.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write notices xlsx", e);
        }
    }

    private static void writeNotices(Sheet sheet, List<NoticeDto> notices, Styles styles) {
        writeHeader(sheet, NOTICE_HEADERS, styles.header);
        int rowIndex = 1;
        for (NoticeDto notice : notices) {
            Row row = sheet.createRow(rowIndex++);
            int col = 0;
            writeString(row, col++, notice.getNumber());
            writeDateTime(row, col++, notice.getPublishedAt(), styles.dateTime);
            writeString(row, col++, notice.getSubject());
            writeString(row, col++, notice.getMethodName());
            writeMoney(row, col++, notice.getNmck(), styles.money);
            writeString(row, col++, customerName(notice));
            writeGeneralNumber(row, col++, size(notice.getLots()));
            writeGeneralNumber(row, col++, size(notice.getItems()));
            writeDateTime(row, col++, notice.getApplicationsTo(), styles.dateTime);
            writeString(row, col++, protocolNumbers(notice));
            ContractDto contract = notice.getContract();
            if (contract == null) {
                col += 7;
                writeString(row, col, notes(notice));
            } else {
                writeString(row, col++, supplierCell(contract.getSupplier()));
                writeString(row, col++, contract.getNumber());
                writeDate(row, col++, contract.getDate(), styles.date);
                writeMoney(row, col++, contract.getPrice(), styles.money);
                writePercent(row, col++, contract.getReductionPct(), styles.percent);
                writeMoney(row, col++, contract.getPaidTotal(), styles.money);
                writeGeneralNumber(row, col++, acceptanceCount(contract));
                writeString(row, col, notes(notice));
            }
        }
        finishSheet(sheet, NOTICE_HEADERS.length, rowIndex);
    }

    private static void writeSpecifications(Sheet sheet, List<NoticeDto> notices, Styles styles) {
        writeHeader(sheet, SPEC_HEADERS, styles.header);
        int rowIndex = 1;
        for (NoticeDto notice : notices) {
            List<SpecPositionDto> items = notice.getItems();
            if (items == null) {
                continue;
            }
            for (SpecPositionDto item : items) {
                Row row = sheet.createRow(rowIndex++);
                int col = 0;
                writeString(row, col++, notice.getNumber());
                writeDateTime(row, col++, notice.getPublishedAt(), styles.dateTime);
                writeGeneralNumber(row, col++, item.getPos());
                writeString(row, col++, item.getName());
                writeString(row, col++, item.getOkpd2());
                writeString(row, col++, item.getKtru());
                writeGeneralNumber(row, col++, item.getQty());
                writeString(row, col++, item.getUnit());
                writeMoney(row, col++, item.getPrice(), styles.money);
                writeMoney(row, col++, item.getAmount(), styles.money);
                writeString(row, col, item.getCharacteristicsText());
            }
        }
        finishSheet(sheet, SPEC_HEADERS.length, rowIndex);
    }

    private static void writeHeader(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void finishSheet(Sheet sheet, int columnCount, int rowCount) {
        sheet.createFreezePane(0, 1);
        int lastRow = Math.max(rowCount - 1, 0);
        sheet.setAutoFilter(new CellRangeAddress(0, lastRow, 0, columnCount - 1));
        DataFormatter formatter = new DataFormatter();
        for (int col = 0; col < columnCount; col++) {
            int maxLen = 0;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                Cell cell = row.getCell(col);
                String text = formatter.formatCellValue(cell);
                if (text != null) {
                    maxLen = Math.max(maxLen, text.length());
                }
            }
            int width = Math.min(Math.max(10, maxLen + 2), 60);
            sheet.setColumnWidth(col, width * 256);
        }
    }

    private static void writeString(Row row, int col, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        row.createCell(col).setCellValue(value);
    }

    private static void writeDateTime(Row row, int col, LocalDateTime value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(col);
        cell.setCellValue(value.truncatedTo(ChronoUnit.MINUTES));
        cell.setCellStyle(style);
    }

    private static void writeDate(Row row, int col, LocalDate value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void writeMoney(Row row, int col, BigDecimal value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(col);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private static void writePercent(Row row, int col, BigDecimal value, CellStyle style) {
        if (value == null) {
            return;
        }
        Cell cell = row.createCell(col);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private static void writeGeneralNumber(Row row, int col, Integer value) {
        if (value == null) {
            return;
        }
        row.createCell(col).setCellValue(value.doubleValue());
    }

    private static void writeGeneralNumber(Row row, int col, int value) {
        row.createCell(col).setCellValue(value);
    }

    private static void writeGeneralNumber(Row row, int col, BigDecimal value) {
        if (value == null) {
            return;
        }
        row.createCell(col).setCellValue(value.doubleValue());
    }

    private static String customerName(NoticeDto notice) {
        OrganizationDto customer = notice.getCustomer();
        return customer == null ? null : customer.getName();
    }

    private static String protocolNumbers(NoticeDto notice) {
        List<ProtocolDto> protocols = notice.getProtocols();
        if (protocols == null || protocols.isEmpty()) {
            return null;
        }
        String joined = protocols.stream()
                .map(ProtocolDto::getNumber)
                .filter(number -> number != null && !number.isBlank())
                .collect(Collectors.joining(", "));
        return joined.isEmpty() ? null : joined;
    }

    private static String supplierCell(OrganizationDto supplier) {
        if (supplier == null) {
            return null;
        }
        String name = supplier.getName() == null ? "" : supplier.getName();
        String inn = supplier.getInn() == null ? "" : supplier.getInn();
        return name + " (ИНН " + inn + ")";
    }

    private static int acceptanceCount(ContractDto contract) {
        List<ExecutionDto> executions = contract.getExecutions();
        if (executions == null) {
            return 0;
        }
        int count = 0;
        for (ExecutionDto execution : executions) {
            if (execution.getKind() == ExecutionKind.ACCEPTANCE) {
                count++;
            }
        }
        return count;
    }

    private static String notes(NoticeDto notice) {
        List<String> parts = new ArrayList<>();
        if (notice.getWarnings() != null) {
            for (String warning : notice.getWarnings()) {
                if (warning != null && !warning.isBlank()) {
                    parts.add(warning);
                }
            }
        }
        if (notice.getContract() == null) {
            parts.add(NO_CONTRACT_NOTE);
        }
        if (parts.isEmpty()) {
            return null;
        }
        return String.join("; ", parts);
    }

    private static int size(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private record Styles(CellStyle header, CellStyle dateTime, CellStyle date, CellStyle money, CellStyle percent) {
        static Styles create(Workbook workbook) {
            DataFormat format = workbook.createDataFormat();

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle header = workbook.createCellStyle();
            header.setFont(bold);
            header.setWrapText(true);
            header.setVerticalAlignment(VerticalAlignment.TOP);

            CellStyle dateTime = workbook.createCellStyle();
            dateTime.setDataFormat(format.getFormat(DATETIME_FORMAT));

            CellStyle date = workbook.createCellStyle();
            date.setDataFormat(format.getFormat(DATE_FORMAT));

            CellStyle money = workbook.createCellStyle();
            money.setDataFormat(format.getFormat(MONEY_FORMAT));

            CellStyle percent = workbook.createCellStyle();
            percent.setDataFormat(format.getFormat(PERCENT_FORMAT));

            return new Styles(header, dateTime, date, money, percent);
        }
    }
}
